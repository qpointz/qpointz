package io.qpointz.mill.ai.langchain4j

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.ToolExecutionResultMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.PartialThinking
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.AgentEvent
import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.CapabilityDependencies
import io.qpointz.mill.ai.CapabilityDependencyContainer
import io.qpointz.mill.ai.CapabilityRegistry
import io.qpointz.mill.ai.ConversationSession
import io.qpointz.mill.ai.MessageRole
import io.qpointz.mill.ai.ProtocolExecutionInput
import io.qpointz.mill.ai.ProtocolMode
import io.qpointz.mill.ai.RunState
import io.qpointz.mill.ai.SchemaAuthoringAgentProfile
import io.qpointz.mill.ai.ToolBinding
import io.qpointz.mill.ai.ToolExecutionContext
import io.qpointz.mill.ai.ToolKind
import io.qpointz.mill.ai.ToolRequest
import io.qpointz.mill.ai.capabilities.schema.SchemaCapabilityDependency
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectCapabilityDependency
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingResolver
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import java.util.concurrent.CompletableFuture

/**
 * Schema agent — iterative native tool loop covering both exploration and authoring.
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
 * 2. If tool calls: execute, append ToolExecutionResultMessage, loop.
 * 3. If a CAPTURE tool ran: observer terminates; synthesis renders via STRUCTURED_FINAL protocol.
 * 4. If no tool calls: `stream()` the final answer using the full message history.
 */
class SchemaExplorationAgent(
    private val model: StreamingChatModel,
    private val schemaService: SchemaFacetService,
    private val dialectSpec: SqlDialectSpec,
    private val sqlQueryDependency: SqlQueryCapabilityDependency,
    private val valueMappingResolver: ValueMappingResolver,
    private val registry: CapabilityRegistry = CapabilityRegistry.load(),
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(kotlinModule()),
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
        listener(AgentEvent.RunStarted(SchemaAuthoringAgentProfile.profile.id))

        val context = buildContext()
        val capabilities = schemaCapabilities(context)
        val bindings = capabilities.flatMap { it.tools }
        val handlerMap = bindings.associateBy { it.spec.name() }
        val toolSpecs = bindings.map { it.spec }

        val messages = mutableListOf<ChatMessage>()
        messages.add(SystemMessage.from(systemPrompt(capabilities)))
        session.messages.forEach { msg ->
            when (msg.role) {
                MessageRole.USER -> messages.add(UserMessage.from(msg.content))
                MessageRole.ASSISTANT -> messages.add(AiMessage.from(msg.content))
                else -> {}
            }
        }
        messages.add(UserMessage.from(input))

        var toolCallCount = 0

        while (toolCallCount < maxToolCallsPerRun) {
            val response = complete(
                ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(toolSpecs)
                    .build()
            )
            val aiMsg = response.aiMessage()

            if (!aiMsg.hasToolExecutionRequests()) {
                // Stream final answer
                val streamMessages = messages.toMutableList()
                if (aiMsg.text() != null) {
                    // Already have the final answer text — re-stream from history
                    val text = aiMsg.text() ?: ""
                    listener(AgentEvent.AnswerCompleted(text))
                    session.appendUserMessage(input)
                    session.appendAssistantMessage(text)
                    return text
                }
                // If no text, stream
                messages.add(aiMsg)
                val finalResponse = stream(streamMessages, listener)
                val text = finalResponse.aiMessage().text() ?: ""
                listener(AgentEvent.AnswerCompleted(text))
                session.appendUserMessage(input)
                session.appendAssistantMessage(text)
                return text
            }

            messages.add(aiMsg)

            var captureBinding: ToolBinding? = null
            for (toolRequest in aiMsg.toolExecutionRequests()) {
                val binding = handlerMap[toolRequest.name()]
                if (binding == null) {
                    messages.add(ToolExecutionResultMessage.from(toolRequest, """{"error":"Unknown tool: ${toolRequest.name()}"}"""))
                    continue
                }
                val args = parseToolArguments(toolRequest.arguments().orEmpty())
                listener(AgentEvent.ToolCall(toolRequest.name(), objectMapper.writeValueAsString(args), toolCallCount))
                val result = binding.handler.invoke(
                    ToolRequest(args, ToolExecutionContext(agentContext = context))
                )
                val resultText = objectMapper.writeValueAsString(result.content)
                listener(AgentEvent.ToolResult(toolRequest.name(), resultText))
                messages.add(ToolExecutionResultMessage.from(toolRequest, resultText))
                toolCallCount++
                if (binding.kind == ToolKind.CAPTURE) {
                    captureBinding = binding
                }
            }

            if (captureBinding != null) {
                // Synthesize via STRUCTURED_FINAL protocol
                val captureProtocol = capabilities
                    .flatMap { it.protocols }
                    .firstOrNull { it.mode == ProtocolMode.STRUCTURED_FINAL }
                if (captureProtocol != null) {
                    val protocolExecutor = LangChain4jProtocolExecutor(model, objectMapper)
                    val runState = RunState(
                        profile = SchemaAuthoringAgentProfile.profile,
                        context = context,
                    )
                    protocolExecutor.execute(
                        ProtocolExecutionInput(
                            protocol = captureProtocol,
                            runState = runState,
                            messages = messages,
                            listener = listener,
                        )
                    )
                }
                session.appendUserMessage(input)
                return ""
            }
        }

        val fallback = "I explored the schema as far as the current run budget allows, but I need a narrower question or target table to continue."
        listener(AgentEvent.AnswerCompleted(fallback))
        session.appendUserMessage(input)
        session.appendAssistantMessage(fallback)
        return fallback
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

    // ── LangChain4j bridge ────────────────────────────────────────────────────

    private fun parseToolArguments(arguments: String): Map<String, Any?> =
        if (arguments.isBlank()) emptyMap()
        else objectMapper.readValue(arguments, object : TypeReference<Map<String, Any?>>() {})

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
        listener: (AgentEvent) -> Unit,
    ): dev.langchain4j.model.chat.response.ChatResponse {
        val future = CompletableFuture<dev.langchain4j.model.chat.response.ChatResponse>()
        val request = ChatRequest.builder().messages(messages).build()
        model.chat(request, object : StreamingChatResponseHandler {
            override fun onPartialResponse(partialResponse: String) {
                listener(AgentEvent.MessageDelta(text = partialResponse))
            }
            override fun onPartialThinking(partialThinking: PartialThinking) {
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
                .parallelToolCalls(true)
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
