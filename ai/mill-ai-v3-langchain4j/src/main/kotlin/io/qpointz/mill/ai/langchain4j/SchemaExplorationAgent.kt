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
import io.qpointz.mill.data.schema.SchemaFacetService
import java.util.concurrent.CompletableFuture

/**
 * Schema exploration agent — iterative ReAct-style tool loop.
 *
 * The agent calls schema tools (list_schemas, list_tables, list_columns, list_relations) in
 * successive rounds until the model decides it has enough information to answer the user.
 * The final synthesis step is streamed so the CLI renders a live response.
 *
 * ## Tool loop
 * 1. `complete()` — non-streaming; model returns tool calls or a final answer
 * 2. If tool calls: execute, append results, loop
 * 3. If no tool calls: `stream()` the synthesis using the full message history
 *
 * ## Factory seam
 * [fromEnv] wires in a [SchemaFacetService] selected by [SchemaFacetServiceFactory] (Option A: demo).
 * Replace the factory call with `SchemaFacetServiceFactory.fromSkymill(...)` for Option B.
 */
class SchemaExplorationAgent(
    private val model: StreamingChatModel,
    private val schemaService: SchemaFacetService,
    private val registry: CapabilityRegistry = CapabilityRegistry.load(),
    private val objectMapper: ObjectMapper = ObjectMapper(),
) {
    private val maxToolCallsPerRun = 8

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
    fun run(input: String, listener: (AgentEvent) -> Unit = {}): String {
        val context = buildContext()
        val capabilities = schemaCapabilities(context)
        val tools = capabilities.flatMap(Capability::tools)
        val toolSpecs = tools.map { ToolSchemaConverter.toToolSpecification(it) }
        val messages = mutableListOf<ChatMessage>(
            SystemMessage.from(systemPrompt(capabilities)),
            UserMessage.from(input),
        )
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
            },
            observer = Observer { observationInput ->
                when {
                    observationInput.runState.toolCallCount >= maxToolCallsPerRun -> Observation(
                        decision = ObservationDecision.STOP_BUDGET,
                        reason = "Schema exploration reached the current tool-call budget.",
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

        return executor.run(
            AgentExecutionInput(
                initialState = RunState(
                    profile = SchemaExplorationAgentProfile.profile,
                    context = context,
                ),
                userInput = input,
                capabilities = capabilities,
                availableTools = tools,
                objectMapper = objectMapper,
            ),
            listener,
        )
    }

    // ── Context ───────────────────────────────────────────────────────────────

    private fun buildContext(): AgentContext = AgentContext(
        contextType = "general",
        capabilityDependencies = CapabilityDependencyContainer.of(
            "schema" to CapabilityDependencies.of(SchemaCapabilityDependency(schemaService))
        ),
    )

    private fun schemaCapabilities(context: AgentContext): List<Capability> {
        val capabilities = registry.capabilitiesFor(SchemaExplorationAgentProfile.profile, context)
        val actualIds = capabilities.map { it.descriptor.id }.toSet()
        require(actualIds == SchemaExplorationAgentProfile.profile.capabilityIds) {
            "Schema-exploration capability mismatch. expected=${SchemaExplorationAgentProfile.profile.capabilityIds} actual=$actualIds"
        }
        return capabilities
    }

    // ── Prompts ───────────────────────────────────────────────────────────────

    private fun systemPrompt(capabilities: List<Capability>): String {
        val promptTexts = capabilities.flatMap { cap ->
            cap.prompts.map { p -> "## ${p.id}\n${p.content}" }
        }
        return buildString {
            appendLine("You are the Mill Schema Exploration agent.")
            appendLine("Help the user understand the data schema by calling schema tools to gather information.")
            appendLine("Call tools iteratively until you have enough information to give a complete answer.")
            appendLine("Be concise and focus on what the user asked.")
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
            registry: CapabilityRegistry = CapabilityRegistry.load(),
        ): SchemaExplorationAgent? {
            val apiKey = System.getenv("OPENAI_API_KEY") ?: return null
            val config = Config(
                apiKey = apiKey,
                modelName = System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini",
                baseUrl = System.getenv("OPENAI_BASE_URL"),
            )
            return fromConfig(config, schemaService, registry)
        }

        fun fromConfig(
            config: Config,
            schemaService: SchemaFacetService,
            registry: CapabilityRegistry = CapabilityRegistry.load(),
        ): SchemaExplorationAgent {
            val builder = OpenAiStreamingChatModel.builder()
                .apiKey(config.apiKey)
                .modelName(config.modelName)
            config.baseUrl?.let(builder::baseUrl)
            return SchemaExplorationAgent(
                model = builder.build(),
                schemaService = schemaService,
                registry = registry,
            )
        }
    }
}
