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
            wire("a1", "sql", mapOf("sql" to "SELECT 1", "dialectId" to "ansi")),
        )
    }

    @Test
    fun shouldMapSqlResultAttachPayload() {
        val record = ArtifactRecord(
            artifactId = "a2",
            conversationId = "c1",
            runId = null,
            kind = "sql.result",
            payload = mapOf(
                "artifactType" to "sql-result",
                "executionId" to "exec-1",
                "rowCount" to 42L,
                "truncated" to false,
                "columns" to listOf(mapOf("name" to "id", "type" to "int")),
            ),
            turnId = "t1",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        assertThat(ArtifactWireMapper.toResponse(record)).isEqualTo(
            wire(
                "a2",
                "data",
                mapOf(
                    "executionId" to "exec-1",
                    "rowCount" to 42L,
                    "truncated" to false,
                    "columns" to listOf(mapOf("name" to "id", "type" to "int")),
                ),
            ),
        )
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
