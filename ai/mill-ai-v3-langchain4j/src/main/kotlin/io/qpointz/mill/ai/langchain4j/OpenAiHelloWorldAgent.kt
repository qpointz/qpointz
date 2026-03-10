package io.qpointz.mill.ai.langchain4j

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.AgentEvent
import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.CapabilityRegistry
import io.qpointz.mill.ai.HelloWorldAgentProfile
import io.qpointz.mill.ai.HelloWorldCapabilitySet
import io.qpointz.mill.ai.Observation
import io.qpointz.mill.ai.ObservationDecision
import io.qpointz.mill.ai.PlannerDecision
import io.qpointz.mill.ai.ToolDefinition
import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ResponseFormatType
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import dev.langchain4j.model.chat.request.json.JsonSchema
import dev.langchain4j.model.chat.response.PartialThinking
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import java.util.concurrent.CompletableFuture

/**
 * Minimal real OpenAI-backed hello-world agent.
 *
 * This intentionally mixes direct response and tool-using flows so the first integration tests
 * exercise the target runtime shape instead of a tool-only demo.
 */
class OpenAiHelloWorldAgent(
    private val model: StreamingChatModel,
    private val registry: CapabilityRegistry = CapabilityRegistry.load(),
    private val objectMapper: ObjectMapper = ObjectMapper(),
) {

    /** Runtime model configuration loaded from environment or tests. */
    data class Config(
        val apiKey: String,
        val modelName: String = "gpt-4o-mini",
        val baseUrl: String? = null,
    )

    /**
     * Execute a single hello-world turn and emit typed runtime events while the model streams.
     */
    fun run(input: String, listener: (AgentEvent) -> Unit = {}): String {
        val context = AgentContext(contextType = "general")
        val capabilities = helloWorldCapabilities(context)
        val tools = capabilities.flatMap(Capability::tools)
        val baseMessages = listOf<ChatMessage>(
            SystemMessage.from(systemPrompt(capabilities)),
            UserMessage.from(input),
        )

        listener(AgentEvent.RunStarted(profileId = HelloWorldAgentProfile.profile.id))
        listener(AgentEvent.ThinkingDelta(message = "Planning response..."))

        val decision = plan(baseMessages, tools)
        listener(AgentEvent.PlanCreated(mode = decision.mode.name, toolName = decision.toolName))

        return when (decision.mode) {
            PlannerDecision.Mode.DIRECT_RESPONSE -> {
                val observation = Observation(
                    decision = ObservationDecision.FINISH,
                    reason = "Planner selected direct response path.",
                )
                listener(AgentEvent.ObservationMade(observation.decision.name, observation.reason))
                val response = stream(baseMessages, emptyList(), listener)
                val text = response.aiMessage().text() ?: ""
                listener(AgentEvent.AnswerCompleted(text = text))
                text
            }

            PlannerDecision.Mode.CALL_TOOL -> {
                val tool = requireNotNull(tools.find { it.name == decision.toolName }) {
                    "Planner selected unknown tool: ${decision.toolName}"
                }
                val rawArguments = objectMapper.writeValueAsString(decision.toolArguments)
                listener(
                    AgentEvent.ToolCall(
                        name = tool.name,
                        arguments = rawArguments,
                        iteration = 0,
                    )
                )
                val resultText = executeTool(tool, rawArguments)
                listener(AgentEvent.ToolResult(name = tool.name, result = resultText))

                val observation = Observation(
                    decision = ObservationDecision.CONTINUE,
                    reason = "Tool result is available and should be synthesized into the final answer.",
                )
                listener(AgentEvent.ObservationMade(observation.decision.name, observation.reason))
                listener(AgentEvent.ThinkingDelta(message = "Synthesizing tool result..."))

                val response = stream(
                    baseMessages + UserMessage.from(
                        "The planner executed tool `${tool.name}` and received this JSON result: $resultText. " +
                            "Use it to answer the user concisely."
                    ),
                    emptyList(),
                    listener,
                )
                val text = response.aiMessage().text() ?: ""
                listener(AgentEvent.AnswerCompleted(text = text))
                text
            }
        }
    }

    /** Resolve and validate the fixed capability set required by the hello-world profile. */
    private fun helloWorldCapabilities(context: AgentContext): List<Capability> {
        val capabilities = registry.capabilitiesFor(context)
            .filter { it.descriptor.id in HelloWorldCapabilitySet.requiredCapabilityIds }

        val actualIds = capabilities.map { it.descriptor.id }.toSet()
        require(actualIds == HelloWorldCapabilitySet.requiredCapabilityIds) {
            "Hello-world capability set mismatch. expected=${HelloWorldCapabilitySet.requiredCapabilityIds} actual=$actualIds"
        }
        return capabilities
    }

    /** Flatten capability prompt assets into a single system prompt for the hello-world run. */
    private fun systemPrompt(capabilities: List<Capability>): String {
        val promptTexts = capabilities.flatMap { capability ->
            capability.prompts.map { prompt ->
                "## Prompt `${prompt.id}`\n${prompt.description}\n${prompt.content}"
            }
        }
        return buildString {
            appendLine("You are the Mill AI hello-world validation agent.")
            appendLine("You are validating a mixed direct-response and tool-using workflow with an explicit planning step.")
            appendLine("Keep replies concise.")
            appendLine()
            appendLine(promptTexts.joinToString("\n\n"))
        }
    }

    /** Ask the model for a structured planning decision before executing the turn. */
    private fun plan(messages: List<ChatMessage>, tools: List<ToolDefinition>): PlannerDecision {
        val planningMessages = listOf(
            SystemMessage.from(
                buildString {
                    appendLine("You are the planner for the Mill AI hello-world validation agent.")
                    appendLine("Decide whether the runtime should answer directly or call exactly one tool.")
                    appendLine("Use DIRECT_RESPONSE for plain greetings or acknowledgements.")
                    appendLine("Use CALL_TOOL when the user explicitly asks to say hello to someone, echo text, run noop, or list capabilities.")
                    appendLine("Available tools:")
                    tools.forEach { tool ->
                        appendLine("- ${tool.name}: ${tool.description}")
                    }
                    appendLine("Return strict JSON matching the provided schema.")
                }
            )
        ) + messages.filterIsInstance<UserMessage>()

        val response = complete(
            ChatRequest.builder()
                .messages(planningMessages)
                .responseFormat(
                    ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(plannerDecisionSchema())
                        .build()
                )
                .build()
        )
        val payload = response.aiMessage().text().orEmpty()
        val node = objectMapper.readTree(payload)
        val toolArguments = node.path("toolArguments")
            .fields()
            .asSequence()
            .associate { (key, value) -> key to value.asText() }

        return PlannerDecision(
            mode = PlannerDecision.Mode.valueOf(node.path("mode").asText()),
            toolName = node.path("toolName").takeUnless { it.isMissingNode || it.isNull }?.asText(),
            toolArguments = toolArguments,
            rationale = node.path("rationale").takeUnless { it.isMissingNode || it.isNull }?.asText(),
        )
    }

    /** Minimal JSON schema for the structured hello-world planning decision. */
    private fun plannerDecisionSchema(): JsonSchema {
        val root = JsonObjectSchema.builder()
            .description("Planning decision for the hello-world validation agent.")
            .addEnumProperty("mode", PlannerDecision.Mode.entries.map { it.name }, "Whether to answer directly or call one tool.")
            .addStringProperty("toolName", "Tool to execute when mode is CALL_TOOL.")
            .addProperty(
                "toolArguments",
                JsonObjectSchema.builder()
                    .description("String arguments passed to the selected tool.")
                    .additionalProperties(true)
                    .build()
            )
            .addStringProperty("rationale", "Short rationale for the chosen mode.")
            .required("mode")
            .additionalProperties(false)
            .build()

        return JsonSchema.builder()
            .name("helloWorldPlannerDecision")
            .rootElement(root)
            .build()
    }

    /** Convert the runtime tool model into LangChain4j tool specifications. */
    private fun toToolSpecification(tool: ToolDefinition): ToolSpecification {
        val parameters = JsonObjectSchema.builder()
        tool.inputFields.forEach { field ->
            parameters.addStringProperty(field.name, field.description)
        }
        val requiredFields = tool.inputFields.filter { it.required }.map { it.name }
        if (requiredFields.isNotEmpty()) {
            parameters.required(requiredFields)
        }

        return ToolSpecification.builder()
            .name(tool.name)
            .description(tool.description)
            .parameters(parameters.build())
            .build()
    }

    /**
     * Execute a trivial demo tool and serialize the structured tool result back to JSON.
     *
     * Returning JSON here keeps the tool bridge close to what later structured agent workflows
     * will need when tool results feed back into the model.
     */
    private fun executeTool(tool: ToolDefinition, rawArguments: String): String {
        val parsed = if (rawArguments.isBlank()) {
            emptyMap()
        } else {
            objectMapper.readValue(rawArguments, object : TypeReference<Map<String, Any?>>() {})
                .mapValues { (_, value) -> value?.toString().orEmpty() }
        }
        return objectMapper.writeValueAsString(tool.handler.invoke(io.qpointz.mill.ai.ToolRequest(parsed)).content)
    }

    /** Execute a non-streaming request used for structured planning decisions. */
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

    /**
     * Bridge LangChain4j streaming callbacks into the runtime event listener.
     *
     * The runtime owns the event model; LangChain4j only supplies streamed model output.
     */
    private fun stream(
        messages: List<ChatMessage>,
        toolSpecifications: List<ToolSpecification>,
        listener: (AgentEvent) -> Unit,
    ): dev.langchain4j.model.chat.response.ChatResponse {
        val future = CompletableFuture<dev.langchain4j.model.chat.response.ChatResponse>()
        val request = ChatRequest.builder()
            .messages(messages)
            .toolSpecifications(toolSpecifications)
            .build()

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

    companion object {
        /** Create the agent from conventional OpenAI environment variables. */
        fun fromEnv(registry: CapabilityRegistry = CapabilityRegistry.load()): OpenAiHelloWorldAgent? {
            val apiKey = System.getenv("OPENAI_API_KEY") ?: return null
            val config = Config(
                apiKey = apiKey,
                modelName = System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini",
                baseUrl = System.getenv("OPENAI_BASE_URL"),
            )
            return fromConfig(config, registry)
        }

        /** Create the agent from an explicit runtime configuration. */
        fun fromConfig(
            config: Config,
            registry: CapabilityRegistry = CapabilityRegistry.load(),
        ): OpenAiHelloWorldAgent {
            val builder = OpenAiStreamingChatModel.builder()
                .apiKey(config.apiKey)
                .modelName(config.modelName)

            config.baseUrl?.let(builder::baseUrl)

            return OpenAiHelloWorldAgent(
                model = builder.build(),
                registry = registry,
            )
        }
    }
}
