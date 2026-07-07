package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.service.dto.ArtifactResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class ArtifactWireMapperTest {

    private fun wire(
        artifactId: String,
        kind: String,
        payload: Map<String, Any?>,
    ) = ArtifactResponse(
        kind = kind,
        payload = payload,
        artifactId = artifactId,
        urn = "urn:agent/artifact:$artifactId",
        status = "active",
    )

    @Test
    fun shouldMapGeneratedSqlProtocolFinal() {
        val record = ArtifactRecord(
            artifactId = "a1",
            conversationId = "c1",
            runId = "r1",
            kind = "sql-query.generated-sql",
            payload = mapOf(
                "protocolId" to "sql-query.generated-sql",
                "payload" to mapOf(
                    "artifactType" to "generated-sql",
                    "sql" to "SELECT 1",
                    "dialectId" to "ansi",
                ),
            ),
            turnId = "t1",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        assertThat(ArtifactWireMapper.toResponse(record)).isEqualTo(
            wire("a1", "sql", mapOf("sql" to "SELECT 1", "dialectId" to "ansi", "artifactType" to "generated-sql")),
        )
    }

    @Test
    fun shouldMapNestedGeneratedSqlWithVisualizations() {
        val record = ArtifactRecord(
            artifactId = "a-nested",
            conversationId = "c1",
            runId = "r1",
            kind = "sql.generated",
            payload = mapOf(
                "protocolId" to "sql-query.generated-sql",
                "payload" to mapOf(
                    "artifactType" to "generated-sql",
                    "sql" to mapOf(
                        "text" to "SELECT country, COUNT(*) c FROM t GROUP BY country",
                        "dialectId" to "CALCITE",
                        "statementKind" to "select",
                        "source" to "generated",
                        "validationWarnings" to emptyList<String>(),
                    ),
                    "info" to mapOf("title" to "By country", "description" to "Counts by country."),
                    "schema" to listOf(mapOf("name" to "country", "type" to "STRING")),
                    "visualizations" to listOf(
                        mapOf(
                            "key" to "default",
                            "kind" to "chart",
                            "chartType" to "bar",
                            "encodings" to mapOf(
                                "category" to mapOf("field" to "country"),
                                "value" to mapOf("field" to "c"),
                            ),
                        ),
                    ),
                ),
            ),
            turnId = "t1",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        val response = ArtifactWireMapper.toResponse(record)
        assertThat(response?.kind).isEqualTo("sql")
        assertThat(response?.payload?.get("visualizations")).isNotNull
        assertThat(response?.payload?.get("info")).isNotNull
    }

    @Test
    fun shouldMapSqlResultAttachPayload_withoutExecutionId() {
        val record = ArtifactRecord(
            artifactId = "a2",
            conversationId = "c1",
            runId = null,
            kind = "sql.result",
            payload = mapOf(
                "artifactType" to "sql-result",
                "sql" to "SELECT id FROM t",
                "rowCount" to 42L,
                "truncated" to false,
                "columns" to listOf(mapOf("name" to "id", "type" to "int")),
                "sourceArtifactId" to "sql-1",
            ),
            turnId = "t1",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        assertThat(ArtifactWireMapper.toResponse(record)).isEqualTo(
            wire(
                "a2",
                "data",
                mapOf(
                    "sql" to "SELECT id FROM t",
                    "rowCount" to 42L,
                    "truncated" to false,
                    "columns" to listOf(mapOf("name" to "id", "type" to "int")),
                    "sourceArtifactId" to "sql-1",
                ),
            ),
        )
    }

    @Test
    fun shouldStripLegacyExecutionId_whenMappingSqlResult() {
        val record = ArtifactRecord(
            artifactId = "a2-legacy",
            conversationId = "c1",
            runId = null,
            kind = "sql.result",
            payload = mapOf(
                "artifactType" to "sql-result",
                "executionId" to "exec-stale",
                "resultId" to "exec-stale",
                "sql" to "SELECT 1",
                "rowCount" to 1L,
            ),
            turnId = "t1",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        val response = ArtifactWireMapper.toResponse(record)
        assertThat(response?.kind).isEqualTo("data")
        assertThat(response?.payload).doesNotContainKey("executionId")
        assertThat(response?.payload).doesNotContainKey("resultId")
        assertThat(response?.payload?.get("sql")).isEqualTo("SELECT 1")
    }

    @Test
    fun shouldMapMetadataFacetingProtocolFinal() {
        val record = ArtifactRecord(
            artifactId = "a-facet",
            conversationId = "c1",
            runId = "r1",
            kind = "metadata.faceting.capture",
            payload = mapOf(
                "protocolId" to "metadata.faceting.capture",
                "persistKind" to "metadata.faceting.capture",
                "payload" to mapOf(
                    "captureType" to "facet_assignment",
                    "facetTypeKey" to "descriptive",
                    "metadataEntityId" to "sales.customers",
                    "serializedPayload" to mapOf("summary" to "VIP customer segment"),
                    "validationWarnings" to emptyList<String>(),
                ),
            ),
            turnId = "t1",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        assertThat(ArtifactWireMapper.toResponse(record)).isEqualTo(
            wire(
                "a-facet",
                "facet-proposal",
                mapOf(
                    "facetTypeKey" to "descriptive",
                    "metadataEntityId" to "sales.customers",
                    "payload" to mapOf("summary" to "VIP customer segment"),
                    "status" to "active",
                ),
            ),
        )
    }

    @Test
    fun shouldMapMetadataFacetingWhenPayloadIsDoubleWrapped() {
        val record = ArtifactRecord(
            artifactId = "a-facet-wrap",
            conversationId = "c1",
            runId = "r1",
            kind = "metadata.faceting.capture",
            payload = mapOf(
                "persistKind" to "metadata.faceting.capture",
                "payload" to mapOf(
                    "protocolId" to "metadata.faceting.capture",
                    "persistKind" to "metadata.faceting.capture",
                    "payload" to mapOf(
                        "facetTypeKey" to "descriptive",
                        "metadataEntityId" to "sales.customers",
                        "serializedPayload" to mapOf("summary" to "VIP"),
                    ),
                ),
            ),
            turnId = "t1",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        assertThat(ArtifactWireMapper.toResponse(record)?.kind).isEqualTo("facet-proposal")
    }

    @Test
    fun shouldMapDeclinedFacetArtifacts_onReplay() {
        val record = ArtifactRecord(
            artifactId = "a-declined",
            conversationId = "c1",
            runId = "r1",
            kind = "metadata.faceting.capture",
            payload = mapOf(
                "payload" to mapOf(
                    "facetTypeKey" to "descriptive",
                    "metadataEntityId" to "urn:mill/model/table:sales.customers",
                    "serializedPayload" to emptyMap<String, Any?>(),
                ),
            ),
            turnId = "t1",
            status = io.qpointz.mill.ai.persistence.ArtifactLifecycleStatus.DECLINED,
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        val response = ArtifactWireMapper.toResponse(record)
        assertThat(response).isNotNull
        assertThat(response!!.status).isEqualTo("rejected")
    }
}
