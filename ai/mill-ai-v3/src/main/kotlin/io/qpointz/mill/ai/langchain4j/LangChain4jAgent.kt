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
import io.qpointz.mill.ai.AgentProfile
import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.CapabilityRegistry
import io.qpointz.mill.ai.ConversationSession
import io.qpointz.mill.ai.MessageRole
import io.qpointz.mill.ai.ProtocolExecutionInput
import io.qpointz.mill.ai.ProtocolMode
import io.qpointz.mill.ai.RunState
import io.qpointz.mill.ai.ToolBinding
import io.qpointz.mill.ai.ToolExecutionContext
import io.qpointz.mill.ai.ToolKind
import io.qpointz.mill.ai.ToolRequest
import java.util.concurrent.CompletableFuture

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
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(kotlinModule()),
) {
    companion object {
        private const val MAX_ITERATIONS = 20

        fun fromEnv(
            profile: AgentProfile,
            registry: CapabilityRegistry = CapabilityRegistry.load(),
        ): LangChain4jAgent? {
            val apiKey = System.getenv("OPENAI_API_KEY") ?: return null
            val config = Config(
                apiKey = apiKey,
                modelName = System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini",
                baseUrl = System.getenv("OPENAI_BASE_URL"),
            )
            return fromConfig(config, profile, registry)
        }

        fun fromConfig(
            config: Config,
            profile: AgentProfile,
            registry: CapabilityRegistry = CapabilityRegistry.load(),
        ): LangChain4jAgent {
            val builder = OpenAiStreamingChatModel.builder()
                .apiKey(config.apiKey)
                .modelName(config.modelName)
                .parallelToolCalls(true)
            config.baseUrl?.let(builder::baseUrl)
            return LangChain4jAgent(
                model = builder.build(),
                profile = profile,
                registry = registry,
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
        listener: (AgentEvent) -> Unit = {},
    ): String {
        listener(AgentEvent.RunStarted(profile.id))

        val context = AgentContext(contextType = "general")
        val capabilities = resolveCapabilities(context)
        val bindings = capabilities.flatMap { it.tools }
        val handlerMap = bindings.associateBy { it.spec.name() }
        val toolSpecs = bindings.map { it.spec }
        val systemPrompt = buildSystemPrompt(capabilities)

        val messages = mutableListOf<ChatMessage>()
        messages.add(SystemMessage.from(systemPrompt))
        session.messages.forEach { msg ->
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
            val aiMsg = response.aiMessage()
            messages.add(aiMsg)

            if (!aiMsg.hasToolExecutionRequests()) {
                // Final answer — stream it
                val text = aiMsg.text() ?: ""
                listener(AgentEvent.AnswerCompleted(text))
                session.appendUserMessage(input)
                session.appendAssistantMessage(text)
                return text
            }

            var captureBinding: ToolBinding? = null
            for (toolRequest in aiMsg.toolExecutionRequests()) {
                val binding = handlerMap[toolRequest.name()] ?: continue
                val args = parseArguments(toolRequest.arguments().orEmpty())
                listener(AgentEvent.ToolCall(toolRequest.name(), objectMapper.writeValueAsString(args), iteration))
                val result = binding.handler.invoke(ToolRequest(args, ToolExecutionContext()))
                val resultText = objectMapper.writeValueAsString(result.content)
                listener(AgentEvent.ToolResult(toolRequest.name(), resultText))
                messages.add(ToolExecutionResultMessage.from(toolRequest, resultText))
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
                    val runState = RunState(profile = profile, context = context)
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

            iteration++
        }

        val fallback = "Reached maximum iteration limit without producing a final answer."
        listener(AgentEvent.AnswerCompleted(fallback))
        session.appendUserMessage(input)
        session.appendAssistantMessage(fallback)
        return fallback
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
                future.complete(completeResponse)
            }
            override fun onError(error: Throwable) {
                future.completeExceptionally(error)
            }
        })
        return future.join()
    }
}
