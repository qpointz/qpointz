package io.qpointz.mill.ai

/**
 * Small generic runtime loop for planner/observer-driven agents.
 *
 * Family-specific agents provide planner, observer, tool execution, and answer synthesis policy
 * while reusing the orchestration mechanics and event emission shape.
 */
class AgentExecutor(
    private val planner: Planner,
    private val observer: Observer,
    private val toolExecutor: ToolCallExecutor,
    private val answerSynthesizer: AnswerSynthesizer,
    private val protocolExecutor: ProtocolExecutor? = null,
    private val clarificationResponder: ClarificationResponder = ClarificationResponder {
        it.question
    },
    private val budgetExceededResponder: BudgetExceededResponder = BudgetExceededResponder {
        "I reached the current exploration budget before I could complete the request."
    },
    private val failureResponder: FailureResponder = FailureResponder {
        it.reason ?: "The run failed before a final answer could be produced."
    },
) {
    fun run(input: AgentExecutionInput, listener: (AgentEvent) -> Unit = {}): String {
        var runState = input.initialState

        listener(AgentEvent.RunStarted(profileId = runState.profile.id))

        while (true) {
            val decision = planner.plan(
                PlannerInput(
                    runState = runState,
                    userInput = input.userInput,
                    capabilities = input.capabilities,
                    availableTools = input.availableTools,
                )
            )
            runState = runState.appendStep(
                RunStep(
                    kind = RunStepKind.PLAN,
                    plannerDecision = decision,
                    summary = decision.rationale,
                    selectedProtocolId = decision.protocolId,
                )
            )
            listener(AgentEvent.PlanCreated(mode = decision.action.name, toolName = decision.toolName))

            when (decision.action) {
                PlannerDecision.Action.DIRECT_RESPONSE,
                PlannerDecision.Action.SYNTHESIZE_ANSWER -> {
                    val observation = observer.observe(
                        ObservationInput(
                            runState = runState,
                            lastPlannerDecision = decision,
                        )
                    )
                    listener(AgentEvent.ObservationMade(observation.decision.name, observation.reason))
                    return when (observation.decision) {
                        ObservationDecision.ANSWER,
                        ObservationDecision.CONTINUE -> synthesize(runState, decision, input, listener)
                        ObservationDecision.CLARIFY -> respondWithClarification(
                            runState = runState,
                            question = decision.clarificationQuestion ?: observation.reason,
                            listener = listener,
                        )
                        ObservationDecision.STOP_BUDGET -> respondWithBudgetExceeded(
                            runState = runState,
                            reason = observation.reason,
                            listener = listener,
                        )
                        ObservationDecision.FAIL -> respondWithFailure(
                            runState = runState,
                            reason = observation.reason,
                            listener = listener,
                        )
                    }
                }

                PlannerDecision.Action.CALL_TOOL -> {
                    val calls = if (decision.toolCalls.isNotEmpty()) {
                        decision.toolCalls
                    } else {
                        listOf(PlannedToolCall(name = requireNotNull(decision.toolName), arguments = decision.toolArguments))
                    }

                    val executedCalls = calls.map { call ->
                        listener(
                            AgentEvent.ToolCall(
                                name = call.name,
                                arguments = input.objectMapper.writeValueAsString(call.arguments),
                                iteration = runState.iteration,
                            )
                        )
                        val result = toolExecutor.execute(
                            ToolCallExecution(
                                runState = runState,
                                toolCall = call,
                            )
                        )
                        runState = runState.appendStep(
                            RunStep(
                                kind = RunStepKind.TOOL_CALL,
                                plannerDecision = decision,
                                toolName = call.name,
                                toolResult = result.resultText,
                                summary = "Executed `${call.name}`.",
                            )
                        )
                        listener(AgentEvent.ToolResult(name = call.name, result = result.resultText))
                        result
                    }

                    val observation = observer.observe(
                        ObservationInput(
                            runState = runState,
                            lastPlannerDecision = decision,
                            toolResult = executedCalls,
                        )
                    )
                    listener(AgentEvent.ObservationMade(observation.decision.name, observation.reason))

                    when (observation.decision) {
                        ObservationDecision.CONTINUE -> Unit
                        ObservationDecision.ANSWER -> return synthesize(runState, decision, input, listener)
                        ObservationDecision.CLARIFY -> {
                            val question = decision.clarificationQuestion
                                ?: observation.nextGoal
                                ?: observation.reason
                            return respondWithClarification(runState, question, listener)
                        }
                        ObservationDecision.STOP_BUDGET ->
                            return respondWithBudgetExceeded(runState, observation.reason, listener)
                        ObservationDecision.FAIL ->
                            return respondWithFailure(runState, observation.reason, listener)
                    }
                }

                PlannerDecision.Action.ASK_CLARIFICATION -> {
                    runState = runState.appendStep(
                        RunStep(
                            kind = RunStepKind.CLARIFICATION,
                            plannerDecision = decision,
                            summary = decision.clarificationQuestion ?: decision.rationale,
                        )
                    )
                    val observation = observer.observe(
                        ObservationInput(
                            runState = runState,
                            lastPlannerDecision = decision,
                        )
                    )
                    listener(AgentEvent.ObservationMade(observation.decision.name, observation.reason))
                    val question = decision.clarificationQuestion
                        ?: observation.nextGoal
                        ?: observation.reason
                    return respondWithClarification(runState, question, listener)
                }

                PlannerDecision.Action.REPLAN -> {
                    listener(AgentEvent.ThinkingDelta(message = decision.rationale ?: "Replanning..."))
                    continue
                }

                PlannerDecision.Action.FAIL -> {
                    val observation = observer.observe(
                        ObservationInput(
                            runState = runState,
                            lastPlannerDecision = decision,
                        )
                    )
                    listener(AgentEvent.ObservationMade(observation.decision.name, observation.reason))
                    return respondWithFailure(runState, observation.reason, listener)
                }
            }
        }
    }

    private fun synthesize(
        runState: RunState,
        decision: PlannerDecision?,
        input: AgentExecutionInput,
        listener: (AgentEvent) -> Unit,
    ): String {
        val protocolId = decision?.protocolId
        val executor = protocolExecutor
        val text = if (protocolId != null && executor != null) {
            val allProtocols = input.capabilities.flatMap { it.protocols }
            val protocol = requireNotNull(allProtocols.find { it.id == protocolId }) {
                "ProtocolExecutor selected protocol '$protocolId' but it is not declared by any capability."
            }
            val result = executor.execute(
                ProtocolExecutionInput(
                    protocol = protocol,
                    runState = runState,
                    messages = input.messages,
                    listener = listener,
                )
            )
            result.text
        } else {
            answerSynthesizer.synthesize(
                AnswerSynthesisInput(
                    runState = runState,
                    listener = listener,
                )
            )
        }
        listener(AgentEvent.AnswerCompleted(text = text))
        return text
    }

    private fun respondWithClarification(
        runState: RunState,
        question: String,
        listener: (AgentEvent) -> Unit,
    ): String {
        val text = clarificationResponder.respond(
            ClarificationInput(
                runState = runState,
                question = question,
                listener = listener,
            )
        )
        listener(AgentEvent.AnswerCompleted(text = text))
        return text
    }

    private fun respondWithBudgetExceeded(
        runState: RunState,
        reason: String,
        listener: (AgentEvent) -> Unit,
    ): String {
        val text = budgetExceededResponder.respond(
            BudgetExceededInput(
                runState = runState,
                reason = reason,
                listener = listener,
            )
        )
        listener(AgentEvent.AnswerCompleted(text = text))
        return text
    }

    private fun respondWithFailure(
        runState: RunState,
        reason: String?,
        listener: (AgentEvent) -> Unit,
    ): String {
        val text = failureResponder.respond(
            FailureInput(
                runState = runState,
                reason = reason,
                listener = listener,
            )
        )
        listener(AgentEvent.AnswerCompleted(text = text))
        return text
    }
}

data class AgentExecutionInput(
    val initialState: RunState,
    val userInput: String,
    val capabilities: List<Capability>,
    val availableTools: List<ToolDefinition>,
    val messages: List<Any> = emptyList(),
    val objectMapper: com.fasterxml.jackson.databind.ObjectMapper = com.fasterxml.jackson.databind.ObjectMapper(),
)

fun interface ToolCallExecutor {
    fun execute(input: ToolCallExecution): ExecutedToolCall
}

data class ToolCallExecution(
    val runState: RunState,
    val toolCall: PlannedToolCall,
)

data class ExecutedToolCall(
    val name: String,
    val arguments: Map<String, Any?> = emptyMap(),
    val resultText: String,
)

fun interface AnswerSynthesizer {
    fun synthesize(input: AnswerSynthesisInput): String
}

data class AnswerSynthesisInput(
    val runState: RunState,
    val listener: (AgentEvent) -> Unit,
)

fun interface ClarificationResponder {
    fun respond(input: ClarificationInput): String
}

data class ClarificationInput(
    val runState: RunState,
    val question: String,
    val listener: (AgentEvent) -> Unit,
)

fun interface BudgetExceededResponder {
    fun respond(input: BudgetExceededInput): String
}

data class BudgetExceededInput(
    val runState: RunState,
    val reason: String,
    val listener: (AgentEvent) -> Unit,
)

fun interface FailureResponder {
    fun respond(input: FailureInput): String
}

data class FailureInput(
    val runState: RunState,
    val reason: String?,
    val listener: (AgentEvent) -> Unit,
)
