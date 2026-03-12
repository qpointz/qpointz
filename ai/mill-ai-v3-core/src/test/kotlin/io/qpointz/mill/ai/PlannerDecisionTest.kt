package io.qpointz.mill.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PlannerDecisionTest {
    @Test
    fun `should create call tool decision with helper`() {
        val decision = PlannerDecision.callTool(
            toolName = "list_tables",
            toolArguments = mapOf("schemaName" to "sales"),
            rationale = "Need schema evidence.",
        )

        assertEquals(PlannerDecision.Action.CALL_TOOL, decision.action)
        assertEquals("list_tables", decision.toolName)
        assertEquals("sales", decision.toolArguments["schemaName"])
    }

    @Test
    fun `should reject tool name on non tool decision`() {
        assertThrows(IllegalArgumentException::class.java) {
            PlannerDecision(
                action = PlannerDecision.Action.DIRECT_RESPONSE,
                toolName = "noop",
            )
        }
    }

    @Test
    fun `should reject clarification question on non clarification decision`() {
        assertThrows(IllegalArgumentException::class.java) {
            PlannerDecision(
                action = PlannerDecision.Action.REPLAN,
                clarificationQuestion = "Which table?",
            )
        }
    }
}
