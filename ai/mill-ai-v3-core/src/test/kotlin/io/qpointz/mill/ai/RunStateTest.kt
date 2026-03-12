package io.qpointz.mill.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RunStateTest {
    @Test
    fun `should track tool and clarification counters from appended steps`() {
        val state = RunState(
            profile = AgentProfile(id = "test", capabilityIds = setOf("conversation")),
            context = AgentContext(contextType = "general"),
        )
            .appendStep(RunStep(kind = RunStepKind.PLAN))
            .appendStep(RunStep(kind = RunStepKind.TOOL_CALL, toolName = "list_schemas"))
            .appendStep(RunStep(kind = RunStepKind.CLARIFICATION))

        assertEquals(3, state.iteration)
        assertEquals(1, state.toolCallCount)
        assertEquals(1, state.clarificationCount)
    }
}
