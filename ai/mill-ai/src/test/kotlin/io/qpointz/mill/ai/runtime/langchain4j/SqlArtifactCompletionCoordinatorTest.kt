package io.qpointz.mill.ai.runtime.langchain4j

import io.qpointz.mill.ai.core.artifact.ArtifactDescriptorRegistry
import io.qpointz.mill.ai.persistence.InMemoryActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.InMemoryArtifactStore
import io.qpointz.mill.ai.persistence.ActiveArtifactPointer
import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.RunState
import io.qpointz.mill.ai.runtime.events.AgentEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class SqlArtifactCompletionCoordinatorTest {

    private val registry = ArtifactDescriptorRegistry.loadDefault()
    private val coordinator = SqlArtifactCompletionCoordinator(registry)
    private val artifactStore = InMemoryArtifactStore()
    private val pointerStore = InMemoryActiveArtifactPointerStore()

    @BeforeEach
    fun setUp() {
        coordinator.reset()
    }

    @Test
    fun shouldFinalizeSqlOnly_afterValidateSqlSucceeds() {
        val events = mutableListOf<AgentEvent>()
        val result = coordinator.processBatch(
            executedTools = listOf(
                tool("validate_sql", mapOf(
                    "passed" to true,
                    "normalizedSql" to "SELECT 1",
                    "title" to "Test query",
                    "description" to "Returns a constant row for testing.",
                    "completionMode" to "sql-only",
                )),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = events::add,
        )
        assertTrue(result.shouldEndTurn)
        val final = events.filterIsInstance<AgentEvent.ProtocolFinal>().single()
        @Suppress("UNCHECKED_CAST")
        val payload = final.payload as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val sql = payload["sql"] as Map<String, Any?>
        assertEquals("SELECT 1", sql["text"])
        @Suppress("UNCHECKED_CAST")
        val info = payload["info"] as Map<String, Any?>
        assertEquals("Test query", info["title"])
    }

    @Test
    fun shouldNotFinalizeSqlWithChart_untilChartStepSucceeds() {
        val events = mutableListOf<AgentEvent>()
        val afterValidate = coordinator.processBatch(
            executedTools = listOf(
                tool("validate_sql", mapOf(
                    "passed" to true,
                    "normalizedSql" to "SELECT country, COUNT(*) c FROM t GROUP BY country",
                    "title" to "By country",
                    "description" to "Counts rows grouped by country column.",
                    "completionMode" to "sql-with-chart",
                )),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = events::add,
        )
        assertFalse(afterValidate.shouldEndTurn)
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolFinal>().isEmpty())

        coordinator.processBatch(
            executedTools = listOf(
                tool("describe_sql", mapOf(
                    "sql" to "SELECT country, COUNT(*) c FROM t GROUP BY country",
                    "schema" to listOf(
                        mapOf("name" to "country", "type" to "STRING"),
                        mapOf("name" to "c", "type" to "BIG_INT"),
                    ),
                )),
                tool("validate_chart_spec", mapOf(
                    "passed" to true,
                    "schemaColumnNames" to listOf("country", "c"),
                    "normalizedVisualization" to mapOf(
                        "key" to "default",
                        "kind" to "chart",
                        "chartType" to "bar",
                        "encodings" to mapOf(
                            "category" to mapOf("field" to "country"),
                            "value" to mapOf("field" to "c"),
                        ),
                    ),
                )),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = events::add,
        )
        val final = events.filterIsInstance<AgentEvent.ProtocolFinal>().single()
        @Suppress("UNCHECKED_CAST")
        val payload = final.payload as Map<String, Any?>
        assertTrue(payload.containsKey("visualizations"))
    }

    @Test
    fun shouldNotPersistSqlWithChart_whenDescribeSqlFails() {
        val events = mutableListOf<AgentEvent>()
        coordinator.processBatch(
            executedTools = listOf(
                tool("validate_sql", mapOf(
                    "passed" to true,
                    "normalizedSql" to "SELECT 1",
                    "title" to "Test query",
                    "description" to "Returns a constant row for testing.",
                    "completionMode" to "sql-with-chart",
                )),
                tool("describe_sql", mapOf("sql" to "SELECT 1", "schema" to emptyList<Any>())),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = events::add,
        )
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolFinal>().isEmpty())
    }

    @Test
    fun shouldFinalizeSqlWithChart_afterChartValidationRetrySucceeds() {
        val events = mutableListOf<AgentEvent>()
        coordinator.processBatch(
            executedTools = listOf(
                tool("validate_sql", mapOf(
                    "passed" to true,
                    "normalizedSql" to "SELECT country, COUNT(*) client_count FROM clients GROUP BY country",
                    "title" to "Clients by country",
                    "description" to "Counts clients grouped by country.",
                    "completionMode" to "sql-with-chart",
                )),
                tool("describe_sql", mapOf(
                    "sql" to "SELECT country, COUNT(*) client_count FROM clients GROUP BY country",
                    "schema" to listOf(
                        mapOf("name" to "country", "type" to "VARCHAR"),
                        mapOf("name" to "client_count", "type" to "BIGINT"),
                    ),
                )),
                tool("validate_chart_spec", mapOf(
                    "passed" to false,
                    "code" to "unknown_field",
                    "message" to "Encoding field 'clients' is not present in schema.",
                )),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = events::add,
        )
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolFinal>().isEmpty())

        coordinator.processBatch(
            executedTools = listOf(
                tool("validate_chart_spec", mapOf(
                    "passed" to true,
                    "schemaColumnNames" to listOf("country", "c"),
                    "normalizedVisualization" to mapOf(
                        "key" to "default",
                        "kind" to "chart",
                        "chartType" to "bar",
                        "encodings" to mapOf(
                            "category" to mapOf("field" to "country"),
                            "value" to mapOf("field" to "client_count"),
                        ),
                    ),
                )),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = events::add,
        )

        val final = events.filterIsInstance<AgentEvent.ProtocolFinal>().single()
        @Suppress("UNCHECKED_CAST")
        val payload = final.payload as Map<String, Any?>
        assertTrue(payload.containsKey("visualizations"))
    }

    @Test
    fun shouldUpdateExistingArtifact_onEnrichExistingSuccess() {
        val existingId = "art-existing"
        artifactStore.save(
            ArtifactRecord(
                artifactId = existingId,
                conversationId = "conv-1",
                runId = "run-old",
                kind = "sql.generated",
                payload = mapOf(
                    "payload" to mapOf(
                        "artifactType" to "generated-sql",
                        "sql" to mapOf(
                            "text" to "SELECT country, COUNT(*) c FROM t GROUP BY country",
                            "dialectId" to "CALCITE",
                            "statementKind" to "select",
                            "source" to "generated",
                            "validationWarnings" to emptyList<String>(),
                        ),
                        "info" to mapOf("title" to "Old", "description" to "Old description for test."),
                        "schema" to listOf(
                            mapOf("name" to "country", "type" to "STRING"),
                            mapOf("name" to "c", "type" to "BIG_INT"),
                        ),
                    ),
                ),
                createdAt = Instant.now(),
            ),
        )
        pointerStore.upsert(
            ActiveArtifactPointer(
                conversationId = "conv-1",
                pointerKey = "last-sql",
                artifactId = existingId,
                updatedAt = Instant.now(),
            ),
        )
        val events = mutableListOf<AgentEvent>()
        coordinator.processBatch(
            executedTools = listOf(
                tool("validate_chart_spec", mapOf(
                    "passed" to true,
                    "targetArtifactId" to existingId,
                    "normalizedVisualization" to mapOf(
                        "key" to "default",
                        "kind" to "chart",
                        "chartType" to "pie",
                        "encodings" to mapOf(
                            "category" to mapOf("field" to "country"),
                            "value" to mapOf("field" to "c"),
                        ),
                    ),
                )),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = events::add,
        )
        val final = events.filterIsInstance<AgentEvent.ProtocolFinal>().single()
        assertEquals(existingId, final.persistArtifactId)
    }

    @Test
    fun shouldNotReplayLastSql_whenChartValidationWithoutEnrichSignal() {
        val existingId = "art-stale"
        artifactStore.save(
            ArtifactRecord(
                artifactId = existingId,
                conversationId = "conv-1",
                runId = "run-old",
                kind = "sql.generated",
                payload = mapOf(
                    "payload" to mapOf(
                        "artifactType" to "generated-sql",
                        "sql" to mapOf(
                            "text" to "SELECT country, COUNT(*) c FROM clients GROUP BY country",
                            "dialectId" to "CALCITE",
                            "statementKind" to "select",
                            "source" to "generated",
                            "validationWarnings" to emptyList<String>(),
                        ),
                        "info" to mapOf("title" to "Countries", "description" to "Top countries."),
                        "schema" to listOf(
                            mapOf("name" to "country", "type" to "STRING"),
                            mapOf("name" to "c", "type" to "BIG_INT"),
                        ),
                    ),
                ),
                createdAt = Instant.now(),
            ),
        )
        pointerStore.upsert(
            ActiveArtifactPointer(
                conversationId = "conv-1",
                pointerKey = "last-sql",
                artifactId = existingId,
                updatedAt = Instant.now(),
            ),
        )
        val events = mutableListOf<AgentEvent>()
        coordinator.processBatch(
            executedTools = listOf(
                tool("validate_chart_spec", mapOf(
                    "passed" to true,
                    "schemaColumnNames" to listOf("country", "c"),
                    "normalizedVisualization" to mapOf(
                        "key" to "default",
                        "kind" to "chart",
                        "chartType" to "bar",
                        "encodings" to mapOf(
                            "category" to mapOf("field" to "exchange"),
                            "value" to mapOf("field" to "cnt"),
                        ),
                    ),
                )),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = events::add,
        )
        assertTrue(events.filterIsInstance<AgentEvent.ProtocolFinal>().isEmpty())
    }

    @Test
    fun shouldRouteChartSpecToPlanMatchingSchema_whenMultipleSqlPlansOpen() {
        coordinator.processBatch(
            executedTools = listOf(
                tool("validate_sql", mapOf(
                    "passed" to true,
                    "normalizedSql" to "SELECT country, COUNT(*) c FROM clients GROUP BY country",
                    "title" to "Countries",
                    "description" to "Top countries.",
                    "completionMode" to "sql-with-chart",
                )),
                tool("validate_sql", mapOf(
                    "passed" to true,
                    "normalizedSql" to "SELECT exchange, COUNT(*) cnt FROM clients GROUP BY exchange",
                    "title" to "Exchanges",
                    "description" to "Top exchanges.",
                    "completionMode" to "sql-with-chart",
                )),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = {},
        )
        coordinator.processBatch(
            executedTools = listOf(
                tool("describe_sql", mapOf(
                    "sql" to "SELECT country, COUNT(*) c FROM clients GROUP BY country",
                    "schema" to listOf(
                        mapOf("name" to "country", "type" to "STRING"),
                        mapOf("name" to "c", "type" to "BIG_INT"),
                    ),
                )),
                tool("describe_sql", mapOf(
                    "sql" to "SELECT exchange, COUNT(*) cnt FROM clients GROUP BY exchange",
                    "schema" to listOf(
                        mapOf("name" to "exchange", "type" to "STRING"),
                        mapOf("name" to "cnt", "type" to "BIG_INT"),
                    ),
                )),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = {},
        )
        val events = mutableListOf<AgentEvent>()
        coordinator.processBatch(
            executedTools = listOf(
                tool("validate_chart_spec", mapOf(
                    "passed" to true,
                    "schemaColumnNames" to listOf("country", "c"),
                    "normalizedVisualization" to mapOf(
                        "key" to "default",
                        "kind" to "chart",
                        "chartType" to "bar",
                        "encodings" to mapOf(
                            "category" to mapOf("field" to "country"),
                            "value" to mapOf("field" to "c"),
                        ),
                    ),
                )),
                tool("validate_chart_spec", mapOf(
                    "passed" to true,
                    "schemaColumnNames" to listOf("cnt", "exchange"),
                    "normalizedVisualization" to mapOf(
                        "key" to "default",
                        "kind" to "chart",
                        "chartType" to "bar",
                        "encodings" to mapOf(
                            "category" to mapOf("field" to "exchange"),
                            "value" to mapOf("field" to "cnt"),
                        ),
                    ),
                )),
            ),
            runState = runState(),
            artifactStore = artifactStore,
            pointerStore = pointerStore,
            listener = events::add,
        )
        val finals = events.filterIsInstance<AgentEvent.ProtocolFinal>()
        assertEquals(2, finals.size)
        val payloads = finals.map { it.payload as Map<String, Any?> }
        val sqlTexts = payloads.map { payload ->
            @Suppress("UNCHECKED_CAST")
            (payload["sql"] as Map<String, Any?>)["text"] as String
        }
        assertTrue(sqlTexts.any { it.contains("country") })
        assertTrue(sqlTexts.any { it.contains("exchange") })
        payloads.forEach { payload ->
            @Suppress("UNCHECKED_CAST")
            val visualizations = payload["visualizations"] as List<Map<String, Any?>>
            assertEquals(1, visualizations.size)
            @Suppress("UNCHECKED_CAST")
            val encodings = visualizations.single()["encodings"] as Map<String, Map<String, Any?>>
            @Suppress("UNCHECKED_CAST")
            val sql = (payload["sql"] as Map<String, Any?>)["text"] as String
            if (sql.contains("country")) {
                assertEquals("country", encodings["category"]?.get("field"))
            } else {
                assertEquals("exchange", encodings["category"]?.get("field"))
            }
        }
    }

    private fun tool(name: String, result: Map<String, Any?>): ArtifactEmissionCoordinator.ExecutedTool =
        ArtifactEmissionCoordinator.ExecutedTool(name, result)

    private fun runState() = RunState(
        profile = AgentProfile(id = "test", capabilityIds = emptySet()),
        context = AgentContext(contextType = "general"),
        conversationId = "conv-1",
    )
}
