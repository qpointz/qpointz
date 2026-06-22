package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.core.artifact.FacetProposalWire
import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.service.dto.ArtifactResponse

/**
 * Maps durable [ArtifactRecord] rows to consumer-safe HTTP DTOs for mill-ui replay.
 */
object ArtifactWireMapper {

    /**
     * Converts a persisted artifact into a wire artefact (`sql`, `data`, `facet-proposal`).
     *
     * @param record durable artifact row
     * @return consumer-safe response or `null` when the payload is not recognised
     */
    fun toResponse(record: ArtifactRecord): ArtifactResponse? {
        val inner = extractInnerPayload(record.payload) ?: return null
        val artifactType = inner["artifactType"] as? String

        when {
            isSqlPayload(inner, record.kind, artifactType) -> {
                val sql = inner["sql"] as? String ?: return null
                if (sql.isBlank()) return null
                return ArtifactResponse(
                    kind = "sql",
                    payload = buildMap {
                        put("sql", sql)
                        (inner["dialectId"] as? String)?.let { put("dialectId", it) }
                    },
                )
            }
            isDataPayload(record.kind, artifactType) -> {
                val executionId = (inner["executionId"] as? String)
                    ?: (inner["resultId"] as? String)
                    ?: return null
                return ArtifactResponse(
                    kind = "data",
                    payload = buildMap {
                        put("executionId", executionId)
                        inner["sql"]?.let { put("sql", it) }
                        inner["rowCount"]?.let { put("rowCount", it) }
                        inner["truncated"]?.let { put("truncated", it) }
                        inner["columns"]?.let { put("columns", it) }
                    },
                )
            }
            isFacetPayload(inner, record.kind, artifactType) -> {
                val wirePayload = FacetProposalWire.normalizePayload(inner) ?: return null
                return ArtifactResponse(
                    kind = FacetProposalWire.WIRE_KIND,
                    payload = wirePayload,
                )
            }
        }
        return null
    }

    private fun extractInnerPayload(content: Map<String, Any?>): Map<String, Any?>? {
        var current: Map<String, Any?> = content
        repeat(4) {
            if (isRecognizedArtifactPayload(current)) return current
            val nested = current["payload"]
            if (nested is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                current = nested as Map<String, Any?>
            } else {
                return current
            }
        }
        return current
    }

    private fun isRecognizedArtifactPayload(map: Map<String, Any?>): Boolean =
        map["sql"] is String ||
            map["executionId"] is String ||
            map["resultId"] is String ||
            (map["facetTypeKey"] is String && map["metadataEntityId"] is String) ||
            (map["captureType"] is String && map["targetEntityId"] is String) ||
            map["artifactType"] is String

    private fun isSqlPayload(
        inner: Map<String, Any?>,
        kind: String,
        artifactType: String?,
    ): Boolean =
        inner["sql"] is String && (
            kind.contains("generated-sql", ignoreCase = true) ||
                kind == "sql.generated" ||
                artifactType == "generated-sql"
            )

    private fun isDataPayload(kind: String, artifactType: String?): Boolean =
        kind == "sql.result" ||
            kind == "sql-result" ||
            artifactType == "sql-result"

    private fun isFacetPayload(
        inner: Map<String, Any?>,
        kind: String,
        artifactType: String?,
    ): Boolean =
        (inner["facetTypeKey"] is String && inner["metadataEntityId"] is String) ||
            (inner["captureType"] is String && inner["targetEntityId"] is String) ||
            kind.contains("metadata.faceting", ignoreCase = true) ||
            artifactType == "metadata-facet-proposal" ||
            FacetProposalWire.isSchemaAuthoringPersistKind(kind, artifactType)
}
