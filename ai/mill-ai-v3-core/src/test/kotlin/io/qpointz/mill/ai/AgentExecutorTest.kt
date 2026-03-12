package io.qpointz.mill.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AgentExecutorTest {
    @Test
    fun `should execute planned tool call and then synthesize when observer answers`() {
        val executor = AgentExecutor(
            planner = Planner {
                if (it.runState.toolCallCount == 0) {
                    PlannerDecision.callTool("list_schemas")
                } else {
                    PlannerDecision(action = PlannerDecision.Action.SYNTHESIZE_ANSWER)
                }
            },
            observer = Observer {
                if (it.toolResult == null) {
                    Observation(decision = ObservationDecision.CONTINUE, reason = "Need tool result.")
                } else {
                    Observation(decision = ObservationDecision.ANSWER, reason = "Enough evidence.")
                }
            },
            toolExecutor = ToolCallExecutor {
                ExecutedToolCall(name = it.toolCall.name, resultText = """["sales"]""")
            },
            answerSynthesizer = AnswerSynthesizer {
                "Schemas: sales"
            },
        )

        val text = executor.run(
            AgentExecutionInput(
                initialState = RunState(
                    profile = AgentProfile(id = "test", capabilityIds = setOf("schema")),
                    context = AgentContext(contextType = "general"),
                ),
                userInput = "What schemas exist?",
                capabilities = emptyList(),
                availableTools = emptyList(),
            )
        )

        assertEquals("Schemas: sales", text)
    }

    @Test
    fun `should return clarification prompt when planner asks for clarification`() {
        val executor = AgentExecutor(
            planner = Planner {
                PlannerDecision.askClarification("Which table do you mean?")
            },
            observer = Observer {
                Observation(decision = ObservationDecision.CLARIFY, reason = "Need clarification.")
            },
            toolExecutor = ToolCallExecutor {
                error("Tool execution should not be reached.")
            },
            answerSynthesizer = AnswerSynthesizer {
                error("Answer synthesis should not be reached.")
            },
        )

        val text = executor.run(
            AgentExecutionInput(
                initialState = RunState(
                    profile = AgentProfile(id = "test", capabilityIds = setOf("schema")),
                    context = AgentContext(contextType = "general"),
                ),
                userInput = "Tell me about customers",
                capabilities = emptyList(),
                availableTools = emptyList(),
            )
        )

        assertEquals("Which table do you mean?", text)
    }

    @Test
    fun `should continue after replan and stop on budget`() {
        val executor = AgentExecutor(
            planner = Planner {
                if (it.runState.iteration == 0) {
                    PlannerDecision(
                        action = PlannerDecision.Action.REPLAN,
                        rationale = "First branch was too weak.",
                    )
                } else {
                    PlannerDecision.callTool("list_relations")
                }
            },
            observer = Observer {
                if (it.toolResult != null) {
                    Observation(decision = ObservationDecision.STOP_BUDGET, reason = "Budget exhausted.")
                } else {
                    Observation(decision = ObservationDecision.CONTINUE, reason = "Continue.")
                }
            },
            toolExecutor = ToolCallExecutor {
                ExecutedToolCall(name = it.toolCall.name, resultText = "[]")
            },
            answerSynthesizer = AnswerSynthesizer {
                error("Answer synthesis should not be reached.")
            },
            budgetExceededResponder = BudgetExceededResponder {
                "Stopped: ${it.reason}"
            },
        )

        val text = executor.run(
            AgentExecutionInput(
                initialState = RunState(
                    profile = AgentProfile(id = "test", capabilityIds = setOf("schema")),
                    context = AgentContext(contextType = "general"),
                ),
                userInput = "Show relations",
                capabilities = emptyList(),
                availableTools = emptyList(),
            )
        )

        assertEquals("Stopped: Budget exhausted.", text)
    }
}
