package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.core.artifact.ArtifactRef
import io.qpointz.mill.ai.core.artifact.FacetProposalWire
import io.qpointz.mill.ai.persistence.ArtifactLifecycleStatus
import io.qpointz.mill.ai.persistence.toWireStatus
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
            isSqlPayload(inner, record.kind, artifactType) ->
                return mapSql(record, inner, artifactType)
            isDataPayload(record.kind, artifactType) ->
                return mapData(record, inner, record.kind, artifactType)
            isFacetPayload(inner, record.kind, artifactType) ->
                return mapFacet(record, inner, record.kind, artifactType)
        }
        return null
    }

    private fun wireEnvelope(
        record: ArtifactRecord,
        kind: String,
        payload: Map<String, Any?>,
    ): ArtifactResponse {
        val ref = record.ref()
        return ArtifactResponse(
            kind = kind,
            payload = payload,
            artifactId = ref.id,
            urn = ref.urn,
            status = record.status.toWireStatus(),
        )
    }

    private fun mapSql(record: ArtifactRecord, inner: Map<String, Any?>, artifactType: String?): ArtifactResponse? {
        val wire = io.qpointz.mill.ai.core.artifact.GeneratedSqlWire.normalizeForWire(inner)
        val sql = wire["sql"] as? String ?: return null
        if (sql.isBlank()) return null
        return wireEnvelope(
            record = record,
            kind = "sql",
            payload = wire,
        )
    }

    private fun mapData(
        record: ArtifactRecord,
        inner: Map<String, Any?>,
        kind: String,
        artifactType: String?,
    ): ArtifactResponse? {
        // Query-engine executionId is ephemeral — never expose it on hydrate (strip legacy rows too).
        val payload = buildMap<String, Any?> {
            inner["sql"]?.let { put("sql", it) }
            inner["rowCount"]?.let { put("rowCount", it) }
            inner["truncated"]?.let { put("truncated", it) }
            inner["columns"]?.let { put("columns", it) }
            (inner["sourceArtifactId"] as? String)?.let { put("sourceArtifactId", it) }
        }
        if (payload.isEmpty()) return null
        return wireEnvelope(
            record = record,
            kind = "data",
            payload = payload,
        )
    }

    private fun mapFacet(
        record: ArtifactRecord,
        inner: Map<String, Any?>,
        kind: String,
        artifactType: String?,
    ): ArtifactResponse? {
        val wirePayload = FacetProposalWire.normalizePayload(inner)?.toMutableMap() ?: return null
        wirePayload["status"] = record.status.toWireStatus()
        (inner["writeScopeUrns"] as? List<*>)?.let { wirePayload["writeScopeUrns"] = it }
        return wireEnvelope(
            record = record,
            kind = FacetProposalWire.WIRE_KIND,
            payload = wirePayload,
        )
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
            map["sql"] is Map<*, *> ||
            map["artifactType"] == "sql-result" ||
            map["columns"] is List<*> ||
            map["sourceArtifactId"] is String ||
            map["rowCount"] is Number ||
            // Legacy persisted rows may still carry executionId; recognise but never emit it.
            map["executionId"] is String ||
            map["resultId"] is String ||
            (map["facetTypeKey"] is String && map["metadataEntityId"] is String) ||
            (map["captureType"] is String && map["targetEntityId"] is String) ||
            map["artifactType"] is String

    private fun isSqlPayload(
        inner: Map<String, Any?>,
        kind: String,
        artifactType: String?,
    ): Boolean {
        val hasSql = inner["sql"] is String || inner["sql"] is Map<*, *>
        return hasSql && (
            kind.contains("generated-sql", ignoreCase = true) ||
                kind == "sql.generated" ||
                artifactType == "generated-sql"
            )
    }

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
