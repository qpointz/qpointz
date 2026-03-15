package io.qpointz.mill.ai.langchain4j

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
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
import io.qpointz.mill.ai.*
import java.util.concurrent.CompletableFuture

/**
 * LangChain4j-backed streaming agent.
 *
 * Generic over any [AgentProfile] — the profile drives capability resolution and the
 * synthesis protocol.  Instantiate directly or via the [fromEnv] / [fromConfig] factories.
 */
class LangChain4jAgent(
    private val model: StreamingChatModel,
    private val profile: AgentProfile,
    private val registry: CapabilityRegistry = CapabilityRegistry.load(),
    private val objectMapper: ObjectMapper = ObjectMapper(),
) {
    /**
     * Generic observer shared across all profiles.
     *
     * Routes on [ToolKind.CAPTURE]: when the just-completed tool is a capture tool the run
     * is terminal and the observer returns [ObservationDecision.ANSWER], letting the runtime
     * synthesize the capture result via the declared protocol. All other tool completions
     * return [ObservationDecision.CONTINUE] so the planner can loop for further grounding.
     *
     * The observer receives the resolved [tools] list via closure to look up [ToolKind]
     * without coupling to specific tool names.
     */
    private fun buildObserver(tools: List<ToolDefinition>): Observer = Observer { input ->
        val failure = input.failure
        @Suppress("UNCHECKED_CAST")
        val executedCalls = input.toolResult as? List<ExecutedToolCall>

        when {
            failure != null -> Observation(
                decision = ObservationDecision.FAIL,
                reason = "Step failed: ${failure.message ?: failure.javaClass.simpleName}",
            )
            input.lastPlannerDecision?.action == PlannerDecision.Action.DIRECT_RESPONSE -> Observation(
                decision = ObservationDecision.ANSWER,
                reason = "Planner selected direct response path.",
            )
            input.lastPlannerDecision?.action == PlannerDecision.Action.CALL_TOOL -> {
                val captureToolRan = executedCalls?.any { call ->
                    tools.find { it.name == call.name }?.kind == ToolKind.CAPTURE
                } == true
                if (captureToolRan) Observation(
                    decision = ObservationDecision.ANSWER,
                    reason = "Capture tool completed — artifact is ready for synthesis.",
                ) else Observation(
                    decision = ObservationDecision.CONTINUE,
                    reason = "Tool result is available and should inform the next planning step.",
                )
            }
            else -> Observation(
                decision = ObservationDecision.FAIL,
                reason = "Observer received unsupported planner action.",
            )
        }
    }

    /** Runtime model configuration. */
    data class Config(
        val apiKey: String,
        val modelName: String = "gpt-4o-mini",
        val baseUrl: String? = null,
    )

    /** Execute a single agent turn and emit typed runtime events while the model streams. */
    fun run(input: String, listener: (AgentEvent) -> Unit = {}): String {
        val context = AgentContext(contextType = "general")
        val capabilities = resolveCapabilities(context)
        val tools = capabilities.flatMap(Capability::tools)
        val baseMessages = listOf<ChatMessage>(
            SystemMessage.from(systemPrompt(capabilities)),
            UserMessage.from(input),
        )
        val executor = AgentExecutor(
            planner = Planner { plannerInput ->
                listener(AgentEvent.ThinkingDelta(message = "Planning response..."))
                planWithModel(plannerInput.userInput, plannerInput.availableTools, capabilities)
            },
            observer = buildObserver(tools),
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
                val toolStep = synthesis.runState.steps.lastOrNull { it.kind == RunStepKind.TOOL_CALL }
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
            protocolExecutor = LangChain4jProtocolExecutor(model, objectMapper),
        )

        return executor.run(
            AgentExecutionInput(
                initialState = RunState(profile = profile, context = context),
                userInput = input,
                capabilities = capabilities,
                availableTools = tools,
                messages = baseMessages,
                objectMapper = objectMapper,
            ),
            listener,
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

    private fun systemPrompt(capabilities: List<Capability>): String {
        val promptTexts = capabilities.flatMap { capability ->
            capability.prompts.map { prompt ->
                "## Prompt `${prompt.id}`\n${prompt.description}\n${prompt.content}"
            }
        }
        return buildString {
            appendLine("You are the Mill AI agent '${profile.id}'.")
            appendLine("Keep replies concise.")
            appendLine()
            appendLine(promptTexts.joinToString("\n\n"))
        }
    }

    private fun planWithModel(
        userInput: String,
        tools: List<ToolDefinition>,
        capabilities: List<Capability> = emptyList(),
    ): PlannerDecision {
        val planningMessages = listOf(
            SystemMessage.from(
                buildString {
                    appendLine("You are the planner for the Mill AI agent '${profile.id}'.")
                    appendLine("Decide whether the runtime should answer directly or call exactly one tool.")
                    appendLine("Use DIRECT_RESPONSE for plain greetings or acknowledgements.")
                    appendLine("Use CALL_TOOL when the user explicitly asks to invoke one of the available tools.")
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
        val task = node.path("task").takeUnless { it.isMissingNode || it.isNull }?.asText()
        val subtype = node.path("subtype").takeUnless { it.isMissingNode || it.isNull }?.asText()

        if (action == PlannerDecision.Action.DIRECT_RESPONSE || toolName == null) {
            return PlannerDecision.directResponse(rationale = rationale).copy(protocolId = "conversation.stream")
        }

        val tool = requireNotNull(tools.find { it.name == toolName }) {
            "Planner selected unknown tool during argument planning: $toolName"
        }
        val toolArguments = planToolArguments(planningMessages, tool)

        if (task == "AUTHOR_METADATA" && subtype != null) {
            val captureProtocolId = capabilities
                .flatMap { it.protocols }
                .firstOrNull { it.mode == ProtocolMode.STRUCTURED_FINAL }
                ?.id
                ?: "schema-authoring.capture"
            return PlannerDecision.authorMetadata(
                subtype = subtype,
                protocolId = captureProtocolId,
                toolName = toolName,
                toolArguments = toolArguments,
                rationale = rationale,
            )
        }

        return PlannerDecision(
            action = action,
            toolName = toolName,
            toolArguments = toolArguments,
            toolCalls = listOf(PlannedToolCall(name = toolName, arguments = toolArguments)),
            rationale = rationale,
            task = task,
            subtype = subtype,
            protocolId = "conversation.stream",
        )
    }

    private fun plannerSelectionSchema(): JsonSchema {
        val root = JsonObjectSchema.builder()
            .description("Tool-selection decision for the agent planner.")
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
            .addStringProperty("task", "Intent task classification (e.g. EXPLORE_SCHEMA, AUTHOR_METADATA). Omit for DIRECT_RESPONSE.")
            .addStringProperty("subtype", "Subtype within the task (e.g. description, relation for AUTHOR_METADATA). Omit when not applicable.")
            .required("mode")
            .additionalProperties(false)
            .build()

        return JsonSchema.builder()
            .name("plannerSelection")
            .rootElement(root)
            .build()
    }

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
                                .rootElement(ToolSchemaConverter.toJsonObjectSchema(tool.inputSchema))
                                .build()
                        )
                        .build()
                )
                .build()
        )

        val payload = response.aiMessage().text().orEmpty()
        return objectMapper.readValue(payload, object : TypeReference<Map<String, Any?>>() {})
    }

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
     * Used by the answerSynthesizer fallback path (tool-aware synthesis).
     * The protocol executor path uses [LangChain4jProtocolExecutor] directly.
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
            config.baseUrl?.let(builder::baseUrl)
            return LangChain4jAgent(
                model = builder.build(),
                profile = profile,
                registry = registry,
            )
        }
    }
}
