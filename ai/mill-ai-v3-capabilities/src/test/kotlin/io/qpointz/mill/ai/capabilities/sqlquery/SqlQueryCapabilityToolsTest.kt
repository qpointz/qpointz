package io.qpointz.mill.ai.capabilities.sqlquery

import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.CapabilityDependencies
import io.qpointz.mill.ai.ToolDefinition
import io.qpointz.mill.ai.ToolInvocationSimulator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SqlQueryCapabilityToolsTest {

    private lateinit var capability: Capability
    private val sim = ToolInvocationSimulator

    @BeforeEach
    fun setup() {
        capability = SqlQueryCapabilityProvider().create(
            AgentContext(contextType = "general"),
            CapabilityDependencies.of(
                SqlQueryCapabilityDependency(
                    validator = MockSqlValidationService(),
                    executor = MockSqlExecutionService(includeResultId = false),
                )
            ),
        )
    }

    @Test
    fun `capability exposes validate and execute tools`() {
        assertEquals(
            setOf("validate_sql", "execute_sql"),
            capability.tools.map { it.name }.toSet(),
        )
    }

    @Test
    fun `capability exposes structured sql protocols`() {
        assertEquals(
            setOf(
                "sql-query.generated-sql",
                "sql-query.validation",
                "sql-query.result-ref",
            ),
            capability.protocols.map { it.id }.toSet(),
        )
    }

    @Test
    fun `validate_sql returns structured wrapper with free text error`() {
        val result = sim.parseMap(
            sim.invoke(
                tool("validate_sql"),
                """{"sql":"select invalid from orders","attempt":2}""",
            ),
        )

        assertEquals("sql-validation", result["artifactType"])
        assertEquals(false, result["passed"])
        assertEquals(2, result["attempt"])
        assertTrue((result["message"] as String).contains("invalid"))
    }

    @Test
    fun `execute_sql returns result reference without embedded rows`() {
        val result = sim.parseMap(
            sim.invoke(
                tool("execute_sql"),
                """{"statementId":"stmt_1","sql":"select * from orders"}""",
            ),
        )

        assertEquals("sql-result", result["artifactType"])
        assertEquals("stmt_1", result["statementId"])
        assertNull(result["resultId"])
        assertTrue(result.containsKey("columns"))
        assertFalse(result.containsKey("rows"))
    }

    private fun tool(name: String): ToolDefinition =
        capability.tools.first { it.name == name }
}
