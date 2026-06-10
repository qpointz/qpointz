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

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
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
import java.util.concurrent.CompletableFuture

import io.qpointz.mill.ai.capabilities.schema.CaptureResult

/**
 * LangChain4j-backed streaming agent.
 *
 * Uses a native tool loop with [ToolExecutionResultMessage] — no custom planner or observer.
 * Generic over any [AgentProfile] — the profile drives capability resolution.
 * Instantiate directly or via the [fromEnv] / [fromConfig] factories.
 */
class LangChain4jAgent(
    private val model: StreamingChatModel,
    private val profile: AgentProfile,
    private val registry: CapabilityRegistry = CapabilityRegistry.load(),
    private val objectMapper: JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .build(),
    private val chatMemoryStore: ChatMemoryStore = InMemoryChatMemoryStore(),
    private val memoryStrategy: LlmMemoryStrategy = BoundedWindowMemoryStrategy(),
    private val persistenceContext: AgentPersistenceContext = AgentPersistenceContext(),
) {
    companion object {
        private const val MAX_ITERATIONS = 20

        fun fromEnv(
            profile: AgentProfile,
            registry: CapabilityRegistry = CapabilityRegistry.load(),
            chatMemoryStore: ChatMemoryStore = InMemoryChatMemoryStore(),
            memoryStrategy: LlmMemoryStrategy = BoundedWindowMemoryStrategy(),
            persistenceContext: AgentPersistenceContext = AgentPersistenceContext(),
        ): LangChain4jAgent? {
            val apiKey = System.getenv("OPENAI_API_KEY") ?: return null
            val config = Config(
                apiKey = apiKey,
                modelName = System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini",
                baseUrl = System.getenv("OPENAI_BASE_URL"),
            )
            return fromConfig(config, profile, registry, chatMemoryStore, memoryStrategy, persistenceContext)
        }

        fun fromConfig(
            config: Config,
            profile: AgentProfile,
            registry: CapabilityRegistry = CapabilityRegistry.load(),
            chatMemoryStore: ChatMemoryStore = InMemoryChatMemoryStore(),
            memoryStrategy: LlmMemoryStrategy = BoundedWindowMemoryStrategy(),
            persistenceContext: AgentPersistenceContext = AgentPersistenceContext(),
        ): LangChain4jAgent {
            val builder = OpenAiStreamingChatModel.builder()
                .apiKey(config.apiKey)
                .modelName(config.modelName)
                .parallelToolCalls(true)
                .baseUrl(resolvedOpenAiBaseUrl(config.baseUrl))
            return LangChain4jAgent(
                model = builder.build(),
                profile = profile,
                registry = registry,
                chatMemoryStore = chatMemoryStore,
                memoryStrategy = memoryStrategy,
                persistenceContext = persistenceContext,
            )
        }
    }

    /** Runtime model configuration. */
    data class Config(
        val apiKey: String,
        val modelName: String = "gpt-4o-mini",
        val baseUrl: String? = null,
    )

    /** Execute a single agent turn using a conversation session and emit typed runtime events. */
    fun run(
        input: String,
        session: ConversationSession = ConversationSession(),
        context: AgentContext = AgentContext(contextType = "general"),
        listener: (AgentEvent) -> Unit = {},
    ): String {
        val runId = UUID.randomUUID().toString()
        val assistantTurnId = UUID.randomUUID().toString()
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

        val capabilities = resolveCapabilities(context)
        val bindings = capabilities.flatMap { it.tools }
        val handlerMap = bindings.associateBy { it.spec.name() }
        val toolSpecs = bindings.map { it.spec }
        val systemPrompt = buildSystemPrompt(capabilities)

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
        messages.add(SystemMessage.from(systemPrompt))
        projected.forEach { msg ->
            when (msg.role) {
                MessageRole.USER -> messages.add(UserMessage.from(msg.content))
                MessageRole.ASSISTANT -> messages.add(AiMessage.from(msg.content))
                else -> {}
            }
        }
        messages.add(UserMessage.from(input))

        var iteration = 0
        while (iteration < MAX_ITERATIONS) {
            val response = complete(
                ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(toolSpecs)
                    .build()
            )
            emitTokenStats(response, routedListener)
            val aiMsg = response.aiMessage()
            messages.add(aiMsg)

            if (!aiMsg.hasToolExecutionRequests()) {
                // Final answer — stream it
                val text = aiMsg.text() ?: ""
                routedListener(AgentEvent.AnswerCompleted(text))
                saveToMemory(session, input, text)
                session.appendUserMessage(input)
                session.appendAssistantMessage(text)
                return text
            }

            var captureBinding: ToolBinding? = null
            var captureValidationFailed = false
            for (toolRequest in aiMsg.toolExecutionRequests()) {
                val binding = handlerMap[toolRequest.name()] ?: continue
                val args = parseArguments(toolRequest.arguments().orEmpty())
                routedListener(AgentEvent.ToolCall(toolRequest.name(), args, iteration))
                val result = binding.handler.invoke(ToolRequest(args, ToolExecutionContext()))
                val resultText = objectMapper.writeValueAsString(result.content)
                routedListener(AgentEvent.ToolResult(toolRequest.name(), result.content))
                messages.add(ToolExecutionResultMessage.from(toolRequest, resultText))
                if (binding.kind == ToolKind.CAPTURE) {
                    val cr = result.content as? CaptureResult
                    if (cr != null && !cr.captureSucceeded) {
                        captureValidationFailed = true
                    }
                    captureBinding = binding
                }
            }

            if (captureBinding != null && !captureValidationFailed) {
                // Synthesize via the protocol declared on the capture tool (if any)
                val captureProtocol = captureBinding.protocolId?.let { pid ->
                    capabilities.flatMap { it.protocols }.firstOrNull { it.id == pid }
                }
                if (captureProtocol != null) {
                    val protocolExecutor = LangChain4jProtocolExecutor(model, objectMapper)
                    val runState = RunState(profile = profile, context = context)
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

            iteration++
        }

        val fallback = "Reached maximum iteration limit without producing a final answer."
        routedListener(AgentEvent.AnswerCompleted(fallback))
        saveToMemory(session, input, fallback)
        session.appendUserMessage(input)
        session.appendAssistantMessage(fallback)
        return fallback
    }

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

    private fun resolveCapabilities(context: AgentContext): List<Capability> {
        val capabilities = registry.capabilitiesFor(profile, context)
        val actualIds = capabilities.map { it.descriptor.id }.toSet()
        require(actualIds == profile.capabilityIds) {
            "Capability set mismatch for profile '${profile.id}'. expected=${profile.capabilityIds} actual=$actualIds"
        }
        return capabilities
    }

    private fun buildSystemPrompt(capabilities: List<Capability>): String {
        val promptTexts = capabilities.flatMap { capability ->
            capability.prompts.map { prompt ->
                "## Prompt `${prompt.id}`\n${prompt.description}\n${prompt.content}"
            }
        }
        return buildString {
            appendLine("You are the Mill AI agent '${profile.id}'.")
            appendLine("Keep replies concise.")
            appendLine()
            if (promptTexts.isNotEmpty()) appendLine(promptTexts.joinToString("\n\n"))
        }
    }

    private fun parseArguments(json: String): Map<String, Any?> =
        if (json.isBlank()) emptyMap()
        else objectMapper.readValue(json, object : TypeReference<Map<String, Any?>>() {})

    private fun emitTokenStats(response: dev.langchain4j.model.chat.response.ChatResponse, listener: (AgentEvent) -> Unit) {
        response.tokenUsage()?.let { usage ->
            listener(AgentEvent.LlmCallCompleted(
                inputTokens  = usage.inputTokenCount()  ?: 0,
                outputTokens = usage.outputTokenCount() ?: 0,
                totalTokens  = usage.totalTokenCount()  ?: 0,
            ))
        }
    }

    private fun complete(request: ChatRequest): dev.langchain4j.model.chat.response.ChatResponse {
        val future = CompletableFuture<dev.langchain4j.model.chat.response.ChatResponse>()
        model.chat(request, object : StreamingChatResponseHandler {
            override fun onPartialResponse(partialResponse: String) = Unit
            override fun onCompleteResponse(completeResponse: dev.langchain4j.model.chat.response.ChatResponse) {
                future.complete(completeResponse)
            }
            override fun onError(error: Throwable) {
                future.completeExceptionally(error)
            }
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
                emitTokenStats(completeResponse, listener)
                future.complete(completeResponse)
            }
            override fun onError(error: Throwable) {
                future.completeExceptionally(error)
            }
        })
        return future.join()
    }
}





