package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.service.dto.ArtifactResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class ArtifactWireMapperTest {

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
            ArtifactResponse(
                kind = "sql",
                payload = mapOf("sql" to "SELECT 1", "dialectId" to "ansi"),
            ),
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
            ArtifactResponse(
                kind = "data",
                payload = mapOf(
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
            ArtifactResponse(
                kind = "facet-proposal",
                payload = mapOf(
                    "facetTypeKey" to "descriptive",
                    "metadataEntityId" to "sales.customers",
                    "payload" to mapOf("summary" to "VIP customer segment"),
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
    fun shouldMapSchemaAuthoringCaptureProtocolFinal_toFacetProposalWire() {
        val record = ArtifactRecord(
            artifactId = "a-schema",
            conversationId = "c1",
            runId = "r1",
            kind = "schema.authoring.capture",
            payload = mapOf(
                "protocolId" to "schema-authoring.capture",
                "persistKind" to "schema.authoring.capture",
                "payload" to mapOf(
                    "captureType" to "description",
                    "targetEntityId" to "skymill.passenger",
                    "targetEntityType" to "TABLE",
                    "serializedPayload" to mapOf("summary" to "Passenger manifest"),
                    "validationWarnings" to emptyList<String>(),
                ),
            ),
            turnId = "t1",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        assertThat(ArtifactWireMapper.toResponse(record)).isEqualTo(
            ArtifactResponse(
                kind = "facet-proposal",
                payload = mapOf(
                    "facetTypeKey" to "descriptive",
                    "metadataEntityId" to "skymill.passenger",
                    "payload" to mapOf("summary" to "Passenger manifest"),
                ),
            ),
        )
    }
}
