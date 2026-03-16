package io.qpointz.mill.ai.langchain4j

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.ToolExecutionResultMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import io.qpointz.mill.ai.*
import io.qpointz.mill.ai.capabilities.schema.SchemaCapabilityDependency
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingResolver
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import java.util.concurrent.CompletableFuture

/**
 * Schema agent — iterative ReAct-style tool loop covering both exploration and authoring.
 *
 * Uses [SchemaAuthoringAgentProfile] which composes the current schema-facing capability set:
 * - `conversation` — base system prompt.
 * - `schema` — grounding tools: list_schemas, list_tables, list_columns, list_relations.
 * - `schema-authoring` — capture tools: capture_description, capture_relation.
 * - `sql-dialect` — SQL dialect conventions and function discovery.
 * - `sql-query` — SQL validation and execution result references.
 *
 * ## Tool loop
 * 1. `complete()` — non-streaming; model returns tool calls or a final answer.
 * 2. If tool calls: execute, append results, loop.
 * 3. If a CAPTURE tool ran: observer terminates with ANSWER; synthesis renders the capture artifact.
 * 4. If no tool calls: `stream()` the synthesis using the full message history.
 *
 * ## Factory seam
 * [fromEnv] wires in a [SchemaFacetService] selected by [SchemaFacetServiceFactory] (Option A: demo).
 * Replace the factory call with `SchemaFacetServiceFactory.fromSkymill(...)` for Option B.
 */
class SchemaExplorationAgent(
    private val model: StreamingChatModel,
    private val schemaService: SchemaFacetService,
    private val dialectSpec: SqlDialectSpec,
    private val sqlQueryDependency: SqlQueryCapabilityDependency,
    private val valueMappingResolver: ValueMappingResolver,
    private val registry: CapabilityRegistry = CapabilityRegistry.load(),
    private val objectMapper: ObjectMapper = ObjectMapper(),
) {
    private val maxToolCallsPerRun = 50

    data class Config(
        val apiKey: String,
        val modelName: String = "gpt-4o-mini",
        val baseUrl: String? = null,
    )

    /**
     * Run a schema exploration turn and emit typed events while the model reasons and calls tools.
     *
     * Returns the final synthesized answer text.
     */
    fun run(session: ConversationSession, input: String, listener: (AgentEvent) -> Unit = {}): String {
        val context = buildContext()
        val capabilities = schemaCapabilities(context)
        val tools = capabilities.flatMap(Capability::tools)
        val toolSpecs = tools.map { ToolSchemaConverter.toToolSpecification(it) }
        val messages = mutableListOf<ChatMessage>()
        messages.add(SystemMessage.from(systemPrompt(capabilities)))
        session.messages.forEach { msg ->
            when (msg.role) {
                MessageRole.USER -> messages.add(UserMessage.from(msg.content))
                MessageRole.ASSISTANT -> messages.add(dev.langchain4j.data.message.AiMessage.from(msg.content))
                else -> {}
            }
        }
        messages.add(UserMessage.from(input))
        val executor = AgentExecutor(
            planner = Planner {
                val response = complete(
                    ChatRequest.builder()
                        .messages(messages)
                        .toolSpecifications(toolSpecs)
                        .build()
                )
                val aiMessage = response.aiMessage()

                if (!aiMessage.hasToolExecutionRequests()) {
                    PlannerDecision(
                        action = PlannerDecision.Action.SYNTHESIZE_ANSWER,
                        rationale = "Model is ready to synthesize a user-facing answer.",
                    )
                } else {
                    val clarificationRequest = aiMessage.toolExecutionRequests()
                        .firstOrNull { it.name() == "request_clarification" }
                    if (clarificationRequest != null) {
                        val args = parseToolArguments(clarificationRequest.arguments().orEmpty())
                        val question = args["question"] as? String
                        if (question != null) {
                            PlannerDecision.askClarification(
                                question = question,
                                rationale = "Entity ambiguous — model requested clarification before capture.",
                            )
                        } else {
                            PlannerDecision(
                                action = PlannerDecision.Action.FAIL,
                                rationale = "request_clarification called without a question field.",
                            )
                        }
                    } else {
                        messages.add(aiMessage)
                        PlannerDecision.callTools(
                            toolCalls = aiMessage.toolExecutionRequests().map { req ->
                                PlannedToolCall(
                                    requestId = req.id(),
                                    name = req.name(),
                                    arguments = parseToolArguments(req.arguments().orEmpty()),
                                )
                            },
                            rationale = "Model requested schema tools before answering.",
                        )
                    }
                }
            },
            observer = Observer { observationInput ->
                @Suppress("UNCHECKED_CAST")
                val executedCalls = observationInput.toolResult as? List<ExecutedToolCall>
                val captureToolRan = executedCalls?.any { call ->
                    tools.find { it.name == call.name }?.kind == ToolKind.CAPTURE
                } == true
                when {
                    observationInput.runState.toolCallCount >= maxToolCallsPerRun -> Observation(
                        decision = ObservationDecision.STOP_BUDGET,
                        reason = "Schema agent reached the current tool-call budget.",
                    )
                    captureToolRan -> Observation(
                        decision = ObservationDecision.ANSWER,
                        reason = "Capture tool completed — artifact is ready for synthesis.",
                    )
                    observationInput.toolResult != null -> Observation(
                        decision = ObservationDecision.CONTINUE,
                        reason = "Tool results were captured and should be fed back into the next planning round.",
                    )
                    observationInput.lastPlannerDecision?.action == PlannerDecision.Action.SYNTHESIZE_ANSWER -> Observation(
                        decision = ObservationDecision.ANSWER,
                        reason = "Schema evidence is sufficient for final synthesis.",
                    )
                    else -> Observation(
                        decision = ObservationDecision.FAIL,
                        reason = "Schema observer received unsupported state.",
                    )
                }
            },
            toolExecutor = ToolCallExecutor { execution ->
                val resultText = executeSchemaToolCall(tools, context, execution.toolCall)
                val request = ToolExecutionRequest.builder()
                    .id(execution.toolCall.requestId)
                    .name(execution.toolCall.name)
                    .arguments(objectMapper.writeValueAsString(execution.toolCall.arguments))
                    .build()
                messages.add(ToolExecutionResultMessage.from(request, resultText))
                ExecutedToolCall(
                    name = execution.toolCall.name,
                    arguments = execution.toolCall.arguments,
                    resultText = resultText,
                )
            },
            answerSynthesizer = AnswerSynthesizer { synthesis ->
                val finalResponse = stream(messages, emptyList(), synthesis.listener)
                finalResponse.aiMessage().text() ?: ""
            },
            budgetExceededResponder = BudgetExceededResponder {
                "I explored the schema as far as the current run budget allows, but I need a narrower question or target table to continue."
            },
        )

        val answer = executor.run(
            AgentExecutionInput(
                initialState = RunState(
                    profile = SchemaAuthoringAgentProfile.profile,
                    context = context,
                ),
                userInput = input,
                capabilities = capabilities,
                availableTools = tools,
                objectMapper = objectMapper,
            ),
            listener,
        )
        session.appendUserMessage(input)
        session.appendAssistantMessage(answer)
        return answer
    }

    // ── Context ───────────────────────────────────────────────────────────────

    private fun buildContext(): AgentContext = AgentContext(
        contextType = "general",
        capabilityDependencies = CapabilityDependencyContainer.of(
            "schema" to CapabilityDependencies.of(SchemaCapabilityDependency(schemaService)),
            "sql-dialect" to CapabilityDependencies.of(SqlDialectCapabilityDependency(dialectSpec)),
            "sql-query" to CapabilityDependencies.of(sqlQueryDependency),
            "value-mapping" to CapabilityDependencies.of(ValueMappingCapabilityDependency(valueMappingResolver)),
        ),
    )

    private fun schemaCapabilities(context: AgentContext): List<Capability> {
        val capabilities = registry.capabilitiesFor(SchemaAuthoringAgentProfile.profile, context)
        val actualIds = capabilities.map { it.descriptor.id }.toSet()
        require(actualIds == SchemaAuthoringAgentProfile.profile.capabilityIds) {
            "Schema-agent capability mismatch. expected=${SchemaAuthoringAgentProfile.profile.capabilityIds} actual=$actualIds"
        }
        return capabilities
    }

    // ── Prompts ───────────────────────────────────────────────────────────────

    private fun systemPrompt(capabilities: List<Capability>): String {
        val promptTexts = capabilities.flatMap { cap ->
            cap.prompts.map { p -> "## ${p.id}\n${p.content}" }
        }
        return buildString {
            appendLine("You are the Mill data agent.")
            appendLine("You handle three kinds of request:")
            appendLine()
            appendLine("1. DATA RETRIEVAL — user wants query results (e.g. 'count orders by customer', 'show top products', 'how many …').")
            appendLine("   - Use schema tools to ground the relevant tables and columns.")
            appendLine("   - Use sql-dialect tools to learn identifier quoting and available functions.")
            appendLine("   - Then follow the sql-query.system instructions exactly: call validate_sql, then execute_sql, then answer.")
            appendLine("   - NEVER write SQL directly in your answer without first calling validate_sql.")
            appendLine()
            appendLine("2. SCHEMA EXPLORATION — user wants to understand the data model (e.g. 'what tables exist', 'what columns does X have').")
            appendLine("   - Call schema tools iteratively until you have enough information, then answer.")
            appendLine()
            appendLine("3. METADATA AUTHORING — user wants to record descriptions or relations.")
            appendLine("   - Ground entity ids via schema tools first.")
            appendLine("   - If the target entity is ambiguous, call request_clarification — do NOT guess.")
            appendLine("   - Once entities are resolved, call capture_description or capture_relation.")
            appendLine("   - When multiple entities are mentioned, emit ALL capture tool calls in a single parallel batch.")
            appendLine("   - Do not stop after the first capture — cover every entity mentioned.")
            appendLine()
            appendLine("Identify which kind of request the user is making before acting. Be concise and focus on what was asked.")
            appendLine()
            if (promptTexts.isNotEmpty()) appendLine(promptTexts.joinToString("\n\n"))
        }
    }

    // ── Tool execution ────────────────────────────────────────────────────────

    private fun executeSchemaToolCall(
        tools: List<ToolDefinition>,
        context: AgentContext,
        call: PlannedToolCall,
    ): String {
        val tool = tools.find { it.name == call.name }
            ?: return """{"error":"Unknown tool: ${call.name}"}"""
        val request = ToolRequest(
            arguments = call.arguments,
            context = ToolExecutionContext(agentContext = context),
        )
        return objectMapper.writeValueAsString(tool.handler.invoke(request).content)
    }

    private fun parseToolArguments(arguments: String): Map<String, Any?> =
        if (arguments.isBlank()) emptyMap()
        else objectMapper.readValue(arguments, object : TypeReference<Map<String, Any?>>() {})

    // ── LangChain4j bridge ────────────────────────────────────────────────────

    private fun complete(request: ChatRequest): dev.langchain4j.model.chat.response.ChatResponse {
        val future = CompletableFuture<dev.langchain4j.model.chat.response.ChatResponse>()
        model.chat(request, object : StreamingChatResponseHandler {
            override fun onPartialResponse(partialResponse: String) = Unit
            override fun onCompleteResponse(completeResponse: dev.langchain4j.model.chat.response.ChatResponse) {
                future.complete(completeResponse)
            }
            override fun onError(error: Throwable) { future.completeExceptionally(error) }
        })
        return future.join()
    }

    private fun stream(
        messages: List<ChatMessage>,
        toolSpecs: List<dev.langchain4j.agent.tool.ToolSpecification>,
        listener: (AgentEvent) -> Unit,
    ): dev.langchain4j.model.chat.response.ChatResponse {
        val future = CompletableFuture<dev.langchain4j.model.chat.response.ChatResponse>()
        val request = ChatRequest.builder().messages(messages).toolSpecifications(toolSpecs).build()
        model.chat(request, object : StreamingChatResponseHandler {
            override fun onPartialResponse(partialResponse: String) {
                listener(AgentEvent.MessageDelta(text = partialResponse))
            }
            override fun onPartialThinking(partialThinking: dev.langchain4j.model.chat.response.PartialThinking) {
                listener(AgentEvent.ReasoningDelta(text = partialThinking.text()))
            }
            override fun onCompleteResponse(completeResponse: dev.langchain4j.model.chat.response.ChatResponse) {
                future.complete(completeResponse)
            }
            override fun onError(error: Throwable) { future.completeExceptionally(error) }
        })
        return future.join()
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun fromEnv(
            schemaService: SchemaFacetService,
            dialectSpec: SqlDialectSpec,
            sqlQueryDependency: SqlQueryCapabilityDependency,
            valueMappingResolver: ValueMappingResolver,
            registry: CapabilityRegistry = CapabilityRegistry.load(),
        ): SchemaExplorationAgent? {
            val apiKey = System.getenv("OPENAI_API_KEY") ?: return null
            val config = Config(
                apiKey = apiKey,
                modelName = System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini",
                baseUrl = System.getenv("OPENAI_BASE_URL"),
            )
            return fromConfig(config, schemaService, dialectSpec, sqlQueryDependency, valueMappingResolver, registry)
        }

        fun fromConfig(
            config: Config,
            schemaService: SchemaFacetService,
            dialectSpec: SqlDialectSpec,
            sqlQueryDependency: SqlQueryCapabilityDependency,
            valueMappingResolver: ValueMappingResolver,
            registry: CapabilityRegistry = CapabilityRegistry.load(),
        ): SchemaExplorationAgent {
            val builder = OpenAiStreamingChatModel.builder()
                .apiKey(config.apiKey)
                .modelName(config.modelName)
            config.baseUrl?.let(builder::baseUrl)
            return SchemaExplorationAgent(
                model = builder.build(),
                schemaService = schemaService,
                dialectSpec = dialectSpec,
                sqlQueryDependency = sqlQueryDependency,
                valueMappingResolver = valueMappingResolver,
                registry = registry,
            )
        }
    }
}
