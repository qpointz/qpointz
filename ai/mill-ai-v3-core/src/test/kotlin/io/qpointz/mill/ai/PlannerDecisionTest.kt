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

    @Test
    fun `should default task and subtype to null`() {
        val decision = PlannerDecision.directResponse()
        assertEquals(null, decision.task)
        assertEquals(null, decision.subtype)
    }

    @Test
    fun `should carry task and subtype on arbitrary decision`() {
        val decision = PlannerDecision.callTool("noop").copy(
            task = "EXPLORE_SCHEMA",
            subtype = null,
        )
        assertEquals("EXPLORE_SCHEMA", decision.task)
        assertEquals(null, decision.subtype)
    }

    @Test
    fun `should create authorMetadata decision with correct fields`() {
        val decision = PlannerDecision.authorMetadata(
            subtype = "description",
            protocolId = "schema-authoring.capture",
            toolName = "capture_description",
            toolArguments = mapOf("targetEntityId" to "retail.orders"),
            rationale = "User requested description.",
        )
        assertEquals(PlannerDecision.Action.CALL_TOOL, decision.action)
        assertEquals("AUTHOR_METADATA", decision.task)
        assertEquals("description", decision.subtype)
        assertEquals("schema-authoring.capture", decision.protocolId)
        assertEquals("capture_description", decision.toolName)
        assertEquals("retail.orders", decision.toolArguments["targetEntityId"])
    }

    @Test
    fun `should set task and subtype independently for relation authoring`() {
        val decision = PlannerDecision.authorMetadata(
            subtype = "relation",
            protocolId = "schema-authoring.capture",
            toolName = "capture_relation",
        )
        assertEquals("AUTHOR_METADATA", decision.task)
        assertEquals("relation", decision.subtype)
        assertEquals("capture_relation", decision.toolName)
    }
}
