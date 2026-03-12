package io.qpointz.mill.ai.langchain4j

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.AgentEvent
import io.qpointz.mill.ai.AgentExecutionInput
import io.qpointz.mill.ai.AgentExecutor
import io.qpointz.mill.ai.AnswerSynthesizer
import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.CapabilityRegistry
import io.qpointz.mill.ai.ExecutedToolCall
import io.qpointz.mill.ai.HelloWorldAgentProfile
import io.qpointz.mill.ai.Observation
import io.qpointz.mill.ai.ObservationInput
import io.qpointz.mill.ai.ObservationDecision
import io.qpointz.mill.ai.Observer
import io.qpointz.mill.ai.Planner
import io.qpointz.mill.ai.PlannerDecision
import io.qpointz.mill.ai.RunState
import io.qpointz.mill.ai.ToolDefinition
import io.qpointz.mill.ai.ToolCallExecutor
import io.qpointz.mill.ai.ToolExecutionContext
import io.qpointz.mill.ai.ToolRequest
import io.qpointz.mill.ai.ToolSchema
import io.qpointz.mill.ai.ToolSchemaType
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
    private val planner: Planner = Planner { input ->
        planWithModel(input.userInput, input.availableTools)
    }

    private val observer: Observer = Observer { input ->
        val failure = input.failure
        when {
            failure != null -> Observation(
                decision = ObservationDecision.FAIL,
                reason = "Step failed: ${failure.message ?: failure.javaClass.simpleName}",
            )
            input.lastPlannerDecision?.action == PlannerDecision.Action.DIRECT_RESPONSE -> Observation(
                decision = ObservationDecision.ANSWER,
                reason = "Planner selected direct response path.",
            )
            input.lastPlannerDecision?.action == PlannerDecision.Action.CALL_TOOL -> Observation(
                decision = ObservationDecision.CONTINUE,
                reason = "Tool result is available and should be synthesized into the final answer.",
            )
            else -> Observation(
                decision = ObservationDecision.FAIL,
                reason = "Observer received unsupported planner action.",
            )
        }
    }

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
        val executor = AgentExecutor(
            planner = Planner { plannerInput ->
                listener(AgentEvent.ThinkingDelta(message = "Planning response..."))
                planWithModel(plannerInput.userInput, plannerInput.availableTools)
            },
            observer = observer,
            toolExecutor = ToolCallExecutor { execution ->
                val tool = requireNotNull(tools.find { it.name == execution.toolCall.name }) {
                    "Planner selected unknown tool: ${execution.toolCall.name}"
                }
                val rawArguments = objectMapper.writeValueAsString(execution.toolCall.arguments)
                val resultText = executeTool(tool, context, rawArguments)
                ExecutedToolCall(
                    name = execution.toolCall.name,
                    arguments = execution.toolCall.arguments,
                    resultText = resultText,
                )
            },
            answerSynthesizer = AnswerSynthesizer { synthesis ->
                val toolStep = synthesis.runState.steps.lastOrNull { it.kind == io.qpointz.mill.ai.RunStepKind.TOOL_CALL }
                val response = if (toolStep?.toolName != null) {
                    stream(
                        baseMessages + UserMessage.from(
                            "The planner executed tool `${toolStep.toolName}` and received this JSON result: ${toolStep.toolResult}. " +
                                "Use the gathered tool evidence to answer the user concisely."
                        ),
                        emptyList(),
                        synthesis.listener,
                    )
                } else {
                    stream(baseMessages, emptyList(), synthesis.listener)
                }
                response.aiMessage().text() ?: ""
            },
        )

        return executor.run(
            AgentExecutionInput(
                initialState = RunState(
                    profile = HelloWorldAgentProfile.profile,
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

    /** Resolve and validate the fixed capability set required by the hello-world profile. */
    private fun helloWorldCapabilities(context: AgentContext): List<Capability> {
        val capabilities = registry.capabilitiesFor(HelloWorldAgentProfile.profile, context)

        val actualIds = capabilities.map { it.descriptor.id }.toSet()
        require(actualIds == HelloWorldAgentProfile.profile.capabilityIds) {
            "Hello-world capability set mismatch. expected=${HelloWorldAgentProfile.profile.capabilityIds} actual=$actualIds"
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
    private fun planWithModel(userInput: String, tools: List<ToolDefinition>): PlannerDecision {
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
        ) + listOf(UserMessage.from(userInput))

        val response = complete(
            ChatRequest.builder()
                .messages(planningMessages)
                .responseFormat(
                    ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(plannerSelectionSchema())
                        .build()
                )
                .build()
        )
        val payload = response.aiMessage().text().orEmpty()
        val node = objectMapper.readTree(payload)
        val action = PlannerDecision.Action.valueOf(node.path("mode").asText())
        val toolName = node.path("toolName").takeUnless { it.isMissingNode || it.isNull }?.asText()
        val rationale = node.path("rationale").takeUnless { it.isMissingNode || it.isNull }?.asText()

        if (action == PlannerDecision.Action.DIRECT_RESPONSE || toolName == null) {
            return PlannerDecision.directResponse(rationale = rationale)
        }

        val tool = requireNotNull(tools.find { it.name == toolName }) {
            "Planner selected unknown tool during argument planning: $toolName"
        }
        val toolArguments = planToolArguments(planningMessages, tool)

        return PlannerDecision(
            action = action,
            toolName = toolName,
            toolArguments = toolArguments,
            rationale = rationale,
        )
    }

    /** Minimal JSON schema for the planner's tool-selection decision. */
    private fun plannerSelectionSchema(): JsonSchema {
        val root = JsonObjectSchema.builder()
            .description("Tool-selection decision for the hello-world validation agent.")
            .addEnumProperty(
                "mode",
                listOf(
                    PlannerDecision.Action.DIRECT_RESPONSE.name,
                    PlannerDecision.Action.CALL_TOOL.name,
                ),
                "Whether to answer directly or call one tool."
            )
            .addStringProperty("toolName", "Tool to execute when mode is CALL_TOOL.")
            .addStringProperty("rationale", "Short rationale for the chosen mode.")
            .required("mode")
            .additionalProperties(false)
            .build()

        return JsonSchema.builder()
            .name("helloWorldPlannerSelection")
            .rootElement(root)
            .build()
    }

    /** Ask the model for typed arguments matching the selected tool's declared schema. */
    private fun planToolArguments(
        messages: List<ChatMessage>,
        tool: ToolDefinition,
    ): Map<String, Any?> {
        if (tool.inputSchema.properties.isEmpty()) {
            return emptyMap()
        }

        val argumentMessages = listOf(
            SystemMessage.from(
                buildString {
                    appendLine("You are generating typed arguments for the selected tool.")
                    appendLine("Selected tool: ${tool.name}")
                    appendLine("Tool description: ${tool.description}")
                    appendLine("Return strict JSON matching the provided schema exactly.")
                }
            )
        ) + messages.filterIsInstance<UserMessage>()

        val response = complete(
            ChatRequest.builder()
                .messages(argumentMessages)
                .responseFormat(
                    ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(
                            JsonSchema.builder()
                                .name("${tool.name}Arguments")
                                .rootElement(toJsonObjectSchema(tool.inputSchema))
                                .build()
                        )
                        .build()
                )
                .build()
        )

        val payload = response.aiMessage().text().orEmpty()
        return objectMapper.readValue(payload, object : TypeReference<Map<String, Any?>>() {})
    }

    /** Convert the runtime tool model into LangChain4j tool specifications. */
    private fun toToolSpecification(tool: ToolDefinition): ToolSpecification =
        ToolSchemaConverter.toToolSpecification(tool)

    private fun toJsonObjectSchema(schema: ToolSchema): JsonObjectSchema =
        ToolSchemaConverter.toJsonObjectSchema(schema)

    private fun toJsonSchemaElement(schema: ToolSchema): dev.langchain4j.model.chat.request.json.JsonSchemaElement =
        ToolSchemaConverter.toJsonSchemaElement(schema)

    /**
     * Execute a trivial demo tool and serialize the structured tool result back to JSON.
     *
     * Returning JSON here keeps the tool bridge close to what later structured agent workflows
     * will need when tool results feed back into the model.
     */
    private fun executeTool(tool: ToolDefinition, agentContext: AgentContext, rawArguments: String): String {
        val parsed = if (rawArguments.isBlank()) {
            emptyMap()
        } else {
            objectMapper.readValue(rawArguments, object : TypeReference<Map<String, Any?>>() {})
        }
        val request = ToolRequest(
            arguments = parsed,
            context = ToolExecutionContext(agentContext = agentContext),
        )
        return objectMapper.writeValueAsString(tool.handler.invoke(request).content)
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
