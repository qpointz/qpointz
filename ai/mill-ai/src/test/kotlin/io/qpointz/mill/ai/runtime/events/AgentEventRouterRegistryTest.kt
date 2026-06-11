package io.qpointz.mill.ai.runtime.events

import io.qpointz.mill.ai.core.artifact.ArtifactDescriptorRegistry
import io.qpointz.mill.ai.runtime.events.routing.DefaultEventRoutingPolicy
import io.qpointz.mill.ai.runtime.events.routing.RoutedEventCategory
import io.qpointz.mill.ai.runtime.events.routing.RoutedEventDestination
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class AgentEventRouterRegistryTest {

    private val router = RegistryAgentEventRouter(ArtifactDescriptorRegistry.loadDefault())
    private val policy = DefaultEventRoutingPolicy.policy

    private fun input(event: AgentEvent) = AgentEventRoutingInput(
        event = event,
        policy = policy,
        conversationId = "conv-1",
        runId = "run-1",
        profileId = "data-analysis",
        timestamp = Instant.parse("2026-01-01T00:00:00Z"),
    )

    @Test
    fun shouldRouteSqlValidationFromToolResult_andGeneratedSqlFromProtocolFinal() {
        val toolRouted = router.route(
            input(
                AgentEvent.ToolResult(
                    name = "validate_sql",
                    result = mapOf(
                        "artifactType" to "sql-validation",
                        "passed" to true,
                        "attempt" to 1,
                        "normalizedSql" to "SELECT 1",
                    ),
                ),
            ),
        )
        val protocolRouted = router.route(
            input(
                AgentEvent.ProtocolFinal(
                    protocolId = "sql-query.generated-sql",
                    payload = mapOf(
                        "artifactType" to "generated-sql",
                        "sql" to "SELECT 1",
                        "dialectId" to "ansi",
                        "statementKind" to "select",
                    ),
                ),
            ),
        )

        val validationArtifacts = toolRouted.filter { it.kind == "sql.validation" }
        val generatedArtifacts = protocolRouted.filter { it.kind == "sql.generated" }
        assertEquals(1, validationArtifacts.size)
        assertEquals(1, generatedArtifacts.size)
        assertEquals(RoutedEventCategory.ARTIFACT, validationArtifacts.single().category)
        assertEquals(RoutedEventCategory.ARTIFACT, generatedArtifacts.single().category)
        assertTrue(generatedArtifacts.single().destinations.contains(RoutedEventDestination.CHAT_STREAM))
        assertEquals(setOf("last-sql"), generatedArtifacts.single().route.rule.artifactPointerKeys)
    }

    @Test
    fun shouldNotProduceDuplicateSqlGenerated_fromToolResultWhenDescriptorIsProtocolFinal() {
        val toolRouted = router.route(
            input(
                AgentEvent.ToolResult(
                    name = "validate_sql",
                    result = mapOf(
                        "artifactType" to "generated-sql",
                        "sql" to "SELECT 1",
                    ),
                ),
            ),
        )
        assertTrue(toolRouted.none { it.kind == "sql.generated" })
        assertEquals(1, toolRouted.size)
    }

    @Test
    fun shouldApplyRegistryPointerKeys_forFacetProtocolFinal() {
        val routed = router.route(
            input(
                AgentEvent.ProtocolFinal(
                    protocolId = "metadata.faceting.capture",
                    payload = mapOf("captureType" to "facet-proposal"),
                ),
            ),
        )
        assertEquals(setOf("last-metadata-facet-proposal"), routed.single().route.rule.artifactPointerKeys)
    }

    @Test
    fun shouldProduceExactlyOneSqlGenerated_onFullSqlTurn() {
        val allKinds = buildList {
            addAll(
                router.route(
                    input(
                        AgentEvent.ToolResult(
                            name = "validate_sql",
                            result = mapOf(
                                "artifactType" to "sql-validation",
                                "passed" to true,
                                "attempt" to 1,
                                "normalizedSql" to "SELECT 1",
                            ),
                        ),
                    ),
                ).map { it.kind },
            )
            addAll(
                router.route(
                    input(
                        AgentEvent.ProtocolFinal(
                            protocolId = "sql-query.generated-sql",
                            payload = mapOf("artifactType" to "generated-sql", "sql" to "SELECT 1"),
                        ),
                    ),
                ).map { it.kind },
            )
        }
        assertEquals(1, allKinds.count { it == "sql.generated" })
        assertEquals(1, allKinds.count { it == "sql.validation" })
    }
}
