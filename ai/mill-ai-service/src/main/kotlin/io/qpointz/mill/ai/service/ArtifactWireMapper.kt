package io.qpointz.mill.ai.service

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
                val facetTypeKey = inner["facetTypeKey"] as? String ?: return null
                val metadataEntityId = inner["metadataEntityId"] as? String ?: return null
                return ArtifactResponse(
                    kind = "facet-proposal",
                    payload = buildMap {
                        put("facetTypeKey", facetTypeKey)
                        put("metadataEntityId", metadataEntityId)
                        (inner["serializedPayload"] ?: inner["payload"])?.let { put("payload", it) }
                    },
                )
            }
        }
        return null
    }

    /**
     * Derives mill-ui layout hint from replay artefacts on a turn.
     *
     * @param artifacts mapped wire artefacts for the turn
     * @return `sql-primary`, `facet-primary`, or `null`
     */
    fun deriveAssistantReplyView(artifacts: List<ArtifactResponse>): String? {
        if (artifacts.any { it.kind == "sql" || it.kind == "data" }) return "sql-primary"
        if (artifacts.any { it.kind == "facet-proposal" }) return "facet-primary"
        return null
    }

    private fun extractInnerPayload(content: Map<String, Any?>): Map<String, Any?>? {
        val nested = content["payload"]
        return when (nested) {
            is Map<*, *> -> @Suppress("UNCHECKED_CAST") (nested as Map<String, Any?>)
            else -> content
        }
    }

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
        inner["facetTypeKey"] is String && inner["metadataEntityId"] is String ||
            kind.contains("metadata.faceting", ignoreCase = true) ||
            artifactType == "metadata-facet-proposal"
}
