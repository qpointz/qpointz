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
    fun shouldDeriveSqlPrimaryWhenSqlPresent() {
        val artifacts = listOf(
            ArtifactResponse("sql", mapOf("sql" to "SELECT 1")),
        )
        assertThat(ArtifactWireMapper.deriveAssistantReplyView(artifacts)).isEqualTo("sql-primary")
    }
}
