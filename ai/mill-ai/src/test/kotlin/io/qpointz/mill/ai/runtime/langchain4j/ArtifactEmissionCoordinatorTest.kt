package io.qpointz.mill.ai.runtime.langchain4j

import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers
import io.qpointz.mill.ai.core.artifact.ArtifactDescriptorRegistry
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.RunState
import io.qpointz.mill.ai.runtime.events.AgentEvent
import io.qpointz.mill.ai.profile.AgentProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArtifactEmissionCoordinatorTest {

    private val registry = ArtifactDescriptorRegistry.loadDefault()
    private val coordinator = ArtifactEmissionCoordinator(registry)

    @Test
    fun shouldConstructGeneratedSqlProtocolFinal_fromValidateSqlResult() {
        val descriptor = registry.descriptorByQualifiedId("sql-query.generated-sql")!!
        val toolResult = SqlQueryToolHandlers.SqlValidationArtifact(
            passed = true,
            attempt = 1,
            message = null,
            normalizedSql = "SELECT 1",
            title = "Constant row",
            description = "Returns a single constant row for smoke testing.",
        )
        val final = coordinator.constructProtocolFinal(descriptor, toolResult, AgentContext(contextType = "general"))
        assertEquals("sql-query.generated-sql", final.protocolId)
        @Suppress("UNCHECKED_CAST")
        val payload = final.payload as Map<String, Any?>
        assertEquals("generated-sql", payload["artifactType"])
        @Suppress("UNCHECKED_CAST")
        val sql = payload["sql"] as Map<String, Any?>
        assertEquals("SELECT 1", sql["text"])
        assertEquals("select", sql["statementKind"])
    }

    @Test
    fun shouldNotEmitProtocolFinal_whenValidateSqlPasses_withoutEmitTrigger() {
        val events = mutableListOf<AgentEvent>()
        val emitted = coordinator.emitOnToolSuccess(
            executedTools = listOf(
                ArtifactEmissionCoordinator.ExecutedTool(
                    name = "validate_sql",
                    result = mapOf(
                        "artifactType" to "sql-validation",
                        "passed" to true,
                        "attempt" to 1,
                        "normalizedSql" to "SELECT 42",
                        "title" to "Count",
                        "description" to "Returns forty-two as a constant value.",
                    ),
                ),
            ),
            runState = RunState(
                profile = AgentProfile(id = "test", capabilityIds = emptySet()),
                context = AgentContext(contextType = "general"),
                conversationId = "conv-1",
            ),
            listener = events::add,
        )
        assertFalse(emitted)
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolFinal>().isEmpty())
    }

    @Test
    fun shouldNotEmit_whenValidateSqlFails() {
        val events = mutableListOf<AgentEvent>()
        val emitted = coordinator.emitOnToolSuccess(
            executedTools = listOf(
                ArtifactEmissionCoordinator.ExecutedTool(
                    name = "validate_sql",
                    result = mapOf(
                        "artifactType" to "sql-validation",
                        "passed" to false,
                        "attempt" to 1,
                        "message" to "syntax error",
                    ),
                ),
            ),
            runState = RunState(
                profile = AgentProfile(id = "test", capabilityIds = emptySet()),
                context = AgentContext(contextType = "general"),
                conversationId = "conv-1",
            ),
            listener = events::add,
        )
        assertFalse(emitted)
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolFinal>().isEmpty())
    }

    @Test
    fun shouldNotEmitGeneratedSql_whenValidationPassedButSqlMissing() {
        val events = mutableListOf<AgentEvent>()
        val emitted = coordinator.emitOnToolSuccess(
            executedTools = listOf(
                ArtifactEmissionCoordinator.ExecutedTool(
                    name = "validate_sql",
                    result = mapOf(
                        "artifactType" to "sql-validation",
                        "passed" to true,
                        "attempt" to 1,
                    ),
                ),
            ),
            runState = RunState(
                profile = AgentProfile(id = "test", capabilityIds = emptySet()),
                context = AgentContext(contextType = "general"),
                conversationId = "conv-1",
            ),
            listener = events::add,
        )
        assertFalse(emitted)
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolFinal>().isEmpty())
    }
}
