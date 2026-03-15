package io.qpointz.mill.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
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

    // ── protocol executor routing ─────────────────────────────────────────────

    private fun capabilityWithProtocol(protocol: ProtocolDefinition): Capability =
        object : Capability {
            override val descriptor = CapabilityDescriptor(
                id = "test-cap",
                name = "Test",
                description = "Test capability.",
            )
            override val prompts = emptyList<PromptAsset>()
            override val tools = emptyList<ToolDefinition>()
            override val protocols = listOf(protocol)
        }

    @Test
    fun `should route synthesis through protocolExecutor when decision carries protocolId`() {
        val protocol = ProtocolDefinition(
            id = "conv.text",
            description = "Text protocol.",
            mode = ProtocolMode.TEXT,
        )
        val events = mutableListOf<AgentEvent>()
        val executor = AgentExecutor(
            planner = Planner {
                PlannerDecision.directResponse(rationale = "Direct.").copy(protocolId = "conv.text")
            },
            observer = Observer {
                Observation(decision = ObservationDecision.ANSWER, reason = "Answer now.")
            },
            toolExecutor = ToolCallExecutor {
                error("Tool execution should not be reached.")
            },
            answerSynthesizer = AnswerSynthesizer {
                error("AnswerSynthesizer should not be reached when protocolExecutor is wired.")
            },
            protocolExecutor = ProtocolExecutor { input ->
                input.listener(AgentEvent.ProtocolTextDelta(protocolId = input.protocol.id, text = "from protocol"))
                ProtocolExecutionResult(text = "protocol answer")
            },
        )

        val text = executor.run(
            AgentExecutionInput(
                initialState = RunState(
                    profile = AgentProfile(id = "test", capabilityIds = setOf("test-cap")),
                    context = AgentContext(contextType = "general"),
                ),
                userInput = "Hello",
                capabilities = listOf(capabilityWithProtocol(protocol)),
                availableTools = emptyList(),
            ),
            listener = { events.add(it) },
        )

        assertEquals("protocol answer", text)
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolTextDelta>().isNotEmpty())
        assertEquals("from protocol", events.filterIsInstance<AgentEvent.ProtocolTextDelta>().first().text)
    }

    @Test
    fun `should fall back to answerSynthesizer when decision has no protocolId`() {
        val executor = AgentExecutor(
            planner = Planner {
                PlannerDecision.directResponse(rationale = "Direct.") // no protocolId
            },
            observer = Observer {
                Observation(decision = ObservationDecision.ANSWER, reason = "Answer now.")
            },
            toolExecutor = ToolCallExecutor {
                error("Tool execution should not be reached.")
            },
            answerSynthesizer = AnswerSynthesizer { "synthesized answer" },
            protocolExecutor = ProtocolExecutor {
                error("ProtocolExecutor should not be reached when protocolId is absent.")
            },
        )

        val text = executor.run(
            AgentExecutionInput(
                initialState = RunState(
                    profile = AgentProfile(id = "test", capabilityIds = emptySet()),
                    context = AgentContext(contextType = "general"),
                ),
                userInput = "Hello",
                capabilities = emptyList(),
                availableTools = emptyList(),
            )
        )

        assertEquals("synthesized answer", text)
    }

    @Test
    fun `should fall back to answerSynthesizer when no protocolExecutor is provided`() {
        val executor = AgentExecutor(
            planner = Planner {
                PlannerDecision.directResponse(rationale = "Direct.").copy(protocolId = "conv.text")
            },
            observer = Observer {
                Observation(decision = ObservationDecision.ANSWER, reason = "Answer now.")
            },
            toolExecutor = ToolCallExecutor { error("not reached") },
            answerSynthesizer = AnswerSynthesizer { "fallback answer" },
            // protocolExecutor intentionally omitted
        )

        val text = executor.run(
            AgentExecutionInput(
                initialState = RunState(
                    profile = AgentProfile(id = "test", capabilityIds = emptySet()),
                    context = AgentContext(contextType = "general"),
                ),
                userInput = "Hello",
                capabilities = emptyList(),
                availableTools = emptyList(),
            )
        )

        assertEquals("fallback answer", text)
    }

    @Test
    fun `should emit AnswerCompleted after protocol synthesis`() {
        val protocol = ProtocolDefinition(id = "conv.text", description = "Text.", mode = ProtocolMode.TEXT)
        val events = mutableListOf<AgentEvent>()

        val executor = AgentExecutor(
            planner = Planner {
                PlannerDecision.directResponse().copy(protocolId = "conv.text")
            },
            observer = Observer {
                Observation(decision = ObservationDecision.ANSWER, reason = "Done.")
            },
            toolExecutor = ToolCallExecutor { error("not reached") },
            answerSynthesizer = AnswerSynthesizer { error("not reached") },
            protocolExecutor = ProtocolExecutor { ProtocolExecutionResult(text = "done") },
        )

        executor.run(
            AgentExecutionInput(
                initialState = RunState(
                    profile = AgentProfile(id = "test", capabilityIds = setOf("test-cap")),
                    context = AgentContext(contextType = "general"),
                ),
                userInput = "Go",
                capabilities = listOf(capabilityWithProtocol(protocol)),
                availableTools = emptyList(),
            ),
            listener = { events.add(it) },
        )

        val completed = events.filterIsInstance<AgentEvent.AnswerCompleted>()
        assertEquals(1, completed.size)
        assertEquals("done", completed.first().text)
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
