package io.qpointz.mill.ai.runtime.langchain4j

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

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
import java.time.Instant
import java.util.UUID
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
    private val chatMemoryStore: ChatMemoryStore = InMemoryChatMemoryStore(),
    private val memoryStrategy: LlmMemoryStrategy = BoundedWindowMemoryStrategy(),
    private val persistenceContext: AgentPersistenceContext = AgentPersistenceContext(),
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
        val runId = UUID.randomUUID().toString()
        val assistantTurnId = UUID.randomUUID().toString()
        val profile = SchemaAuthoringAgentProfile.profile
        val publisher = persistenceContext.publisher
        val conversationStore = persistenceContext.conversationStore

        conversationStore.ensureExists(session.conversationId, profile.id)
        conversationStore.appendTurn(
            session.conversationId,
            ConversationTurn(
                turnId = UUID.randomUUID().toString(),
                role = "user",
                text = input,
                createdAt = Instant.now(),
            )
        )

        val routedListener: (AgentEvent) -> Unit = { event ->
            listener(event)
            val routingInput = AgentEventRoutingInput(
                event = event,
                policy = profile.routingPolicy,
                conversationId = session.conversationId,
                runId = runId,
                profileId = profile.id,
                turnId = assistantTurnId,
            )
            DefaultAgentEventRouter.route(routingInput).forEach { publisher.publish(it) }
        }

        routedListener(AgentEvent.RunStarted(profile.id))

        val context = buildContext()
        val capabilities = schemaCapabilities(context)
        val bindings = capabilities.flatMap { it.tools }
        val handlerMap = bindings.associateBy { it.spec.name() }
        val toolSpecs = bindings.map { it.spec }

        val memory = chatMemoryStore.load(session.conversationId)
        val projected = memoryStrategy.project(
            MemoryProjectionInput(
                conversationId = session.conversationId,
                profileId = session.profileId,
                memory = memory,
                latestUserInput = input,
            )
        )
        val messages = mutableListOf<ChatMessage>()
        messages.add(SystemMessage.from(systemPrompt(capabilities)))
        projected.forEach { msg ->
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
            emitTokenStats(response, routedListener)
            val aiMsg = response.aiMessage()

            if (!aiMsg.hasToolExecutionRequests()) {
                // Stream final answer
                val streamMessages = messages.toMutableList()
                if (aiMsg.text() != null) {
                    // Already have the final answer text — re-stream from history
                    val text = aiMsg.text() ?: ""
                    routedListener(AgentEvent.AnswerCompleted(text))
                    saveToMemory(session, input, text)
                    session.appendUserMessage(input)
                    session.appendAssistantMessage(text)
                    return text
                }
                // If no text, stream
                messages.add(aiMsg)
                val finalResponse = stream(streamMessages, routedListener)
                val text = finalResponse.aiMessage().text() ?: ""
                routedListener(AgentEvent.AnswerCompleted(text))
                saveToMemory(session, input, text)
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
                routedListener(AgentEvent.ToolCall(toolRequest.name(), args, toolCallCount))
                val result = binding.handler.invoke(
                    ToolRequest(args, ToolExecutionContext(agentContext = context))
                )
                val resultText = objectMapper.writeValueAsString(result.content)
                routedListener(AgentEvent.ToolResult(toolRequest.name(), result.content))
                messages.add(ToolExecutionResultMessage.from(toolRequest, resultText))
                toolCallCount++
                if (binding.kind == ToolKind.CAPTURE) {
                    captureBinding = binding
                }
            }

            if (captureBinding != null) {
                // Synthesize via the protocol declared on the capture tool (if any)
                val captureProtocol = captureBinding.protocolId?.let { pid ->
                    capabilities.flatMap { it.protocols }.firstOrNull { it.id == pid }
                }
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
                            listener = routedListener,
                        )
                    )
                }
                // Emit a transcript turn even on the capture path so the assistant turn exists
                // and artifacts persisted with this turnId have an owning turn to link to.
                routedListener(AgentEvent.AnswerCompleted(""))
                saveToMemory(session, input, "")
                session.appendUserMessage(input)
                return ""
            }
        }

        val fallback = "I explored the schema as far as the current run budget allows, but I need a narrower question or target table to continue."
        routedListener(AgentEvent.AnswerCompleted(fallback))
        saveToMemory(session, input, fallback)
        session.appendUserMessage(input)
        session.appendAssistantMessage(fallback)
        return fallback
    }

    // ── Memory ────────────────────────────────────────────────────────────────

    private fun saveToMemory(session: ConversationSession, userInput: String, assistantResponse: String) {
        val existing = chatMemoryStore.load(session.conversationId)
        val newMessages = buildList {
            addAll(existing?.messages ?: emptyList())
            add(ConversationMessage(MessageRole.USER, userInput))
            if (assistantResponse.isNotEmpty()) add(ConversationMessage(MessageRole.ASSISTANT, assistantResponse))
        }
        chatMemoryStore.save(
            ConversationMemory(
                conversationId = session.conversationId,
                profileId = session.profileId,
                messages = newMessages,
            )
        )
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
            appendLine("   - Then follow the sql-query.system instructions exactly: call validate_sql, then execute_sql.")
            appendLine("   - NEVER write SQL directly in your answer without first calling validate_sql.")
            appendLine("   - After execute_sql completes successfully, stop. Do NOT produce a conversational summary — the query result is the answer.")
            appendLine()
            appendLine("2. SCHEMA EXPLORATION — user wants to understand the data model (e.g. 'what tables exist', 'what columns does X have').")
            appendLine("   - Call schema tools iteratively until you have enough information, then answer.")
            appendLine("   - Answer in plain conversational prose. Do NOT output raw JSON.")
            appendLine()
            appendLine("3. METADATA AUTHORING — user wants to record descriptions or relations.")
            appendLine("   - Ground entity ids via schema tools first.")
            appendLine("   - If the target entity is ambiguous, call request_clarification — do NOT guess.")
            appendLine("   - Once entities are resolved, call capture_description or capture_relation.")
            appendLine("   - When multiple entities are mentioned, emit ALL capture tool calls in a single parallel batch.")
            appendLine("   - Do not stop after the first capture — cover every entity mentioned.")
            appendLine()
            appendLine("Identify which kind of request the user is making before acting. Be concise and focus on what was asked.")
            appendLine("Always answer in plain conversational prose unless the user explicitly asks for a structured format.")
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

    private fun emitTokenStats(response: dev.langchain4j.model.chat.response.ChatResponse, listener: (AgentEvent) -> Unit) {
        response.tokenUsage()?.let { usage ->
            listener(AgentEvent.LlmCallCompleted(
                inputTokens  = usage.inputTokenCount()  ?: 0,
                outputTokens = usage.outputTokenCount() ?: 0,
                totalTokens  = usage.totalTokenCount()  ?: 0,
            ))
        }
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
                emitTokenStats(completeResponse, listener)
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
            chatMemoryStore: ChatMemoryStore = InMemoryChatMemoryStore(),
            memoryStrategy: LlmMemoryStrategy = BoundedWindowMemoryStrategy(),
            persistenceContext: AgentPersistenceContext = AgentPersistenceContext(),
        ): SchemaExplorationAgent? {
            val apiKey = System.getenv("OPENAI_API_KEY") ?: return null
            val config = Config(
                apiKey = apiKey,
                modelName = System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini",
                baseUrl = System.getenv("OPENAI_BASE_URL"),
            )
            return fromConfig(config, schemaService, dialectSpec, sqlQueryDependency, valueMappingResolver, registry, chatMemoryStore, memoryStrategy, persistenceContext)
        }

        fun fromConfig(
            config: Config,
            schemaService: SchemaFacetService,
            dialectSpec: SqlDialectSpec,
            sqlQueryDependency: SqlQueryCapabilityDependency,
            valueMappingResolver: ValueMappingResolver,
            registry: CapabilityRegistry = CapabilityRegistry.load(),
            chatMemoryStore: ChatMemoryStore = InMemoryChatMemoryStore(),
            memoryStrategy: LlmMemoryStrategy = BoundedWindowMemoryStrategy(),
            persistenceContext: AgentPersistenceContext = AgentPersistenceContext(),
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
                chatMemoryStore = chatMemoryStore,
                memoryStrategy = memoryStrategy,
                persistenceContext = persistenceContext,
            )
        }
    }
}





