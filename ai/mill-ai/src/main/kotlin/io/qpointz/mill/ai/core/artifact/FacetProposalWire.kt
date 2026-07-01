package io.qpointz.mill.ai.core.artifact

/**
 * Normalizes facet-like capture payloads to the consumer wire shape for chat (`facet-proposal`).
 *
 * Schema authoring persists `captureType` / `targetEntityId`; metadata authoring already uses
 * `facetTypeKey` / `metadataEntityId`. Chat wire exposes a single kind for all facet proposals.
 */
object FacetProposalWire {

    /** Wire artefact kind for all facet proposals in chat (SSE and GET replay). */
    const val WIRE_KIND: String = "facet-proposal"

    /** Protocol id for schema authoring structured finals. */
    const val SCHEMA_CAPTURE_PROTOCOL_ID: String = "schema-authoring.capture"

    /** Protocol id for metadata authoring structured finals. */
    const val METADATA_FACETING_CAPTURE_PROTOCOL_ID: String = "metadata.faceting.capture"

    /**
     * Maps schema capture subtypes to metadata facet-type keys for descriptor lookup.
     *
     * @param captureType schema capture subtype (e.g. `description`, `relation`)
     * @return facet type key (e.g. `descriptive`, `relation`)
     */
    fun facetTypeKeyFromCaptureType(captureType: String): String = when (captureType.trim().lowercase()) {
        "description" -> "descriptive"
        "relation" -> "relation"
        else -> captureType.trim()
    }

    /**
     * Normalizes a single structured-final item for chat wire using the descriptor [wirePartType].
     *
     * @param wirePartType descriptor wire part type (e.g. `facet-proposal`, `sql`)
     * @param payload one item from a scalar or batch protocol final
     * @return wire-ready payload map, or `null` when normalization fails
     */
    fun normalizeForWire(wirePartType: String?, payload: Map<String, Any?>): Any? =
        when (wirePartType) {
            WIRE_KIND -> normalizePayload(payload)
            else -> payload
        }

    /**
     * Converts a protocol or persist payload to the facet-proposal wire map, or `null` when invalid.
     *
     * @param payload structured final or nested persist payload
     * @return wire payload with `facetTypeKey`, `metadataEntityId`, optional `payload`
     */
    @Suppress("UNCHECKED_CAST")
    fun normalizePayload(payload: Any?): Map<String, Any?>? {
        if (payload == null) {
            return null
        }
        val map = payload as? Map<String, Any?> ?: return null

        val existingFacetTypeKey = map["facetTypeKey"] as? String
        val existingEntityId = map["metadataEntityId"] as? String
        if (!existingFacetTypeKey.isNullOrBlank() && !existingEntityId.isNullOrBlank()) {
            return buildMap {
                put("facetTypeKey", existingFacetTypeKey)
                put("metadataEntityId", existingEntityId)
                (map["catalogPath"] as? String)?.let { put("catalogPath", it) }
                (map["rationale"] as? String)?.takeIf { it.isNotBlank() }?.let { put("rationale", it) }
                (map["serializedPayload"] ?: map["payload"])?.let { put("payload", it) }
                (map["candidateConceptLinks"] as? List<*>)?.takeIf { it.isNotEmpty() }?.let {
                    put("candidateConceptLinks", it)
                }
            }
        }

        val captureType = map["captureType"] as? String ?: return null
        val targetEntityId = map["targetEntityId"] as? String ?: return null
        if (map["captureSucceeded"] == false) {
            return null
        }

        val facetTypeKey = when (captureType.trim().lowercase()) {
            "facet_assignment" -> map["facetTypeKey"] as? String ?: return null
            else -> facetTypeKeyFromCaptureType(captureType)
        }

        return buildMap {
            put("facetTypeKey", facetTypeKey)
            put("metadataEntityId", targetEntityId)
            (map["catalogPath"] as? String)?.let { put("catalogPath", it) }
            (map["rationale"] as? String)?.takeIf { it.isNotBlank() }?.let { put("rationale", it) }
            (map["serializedPayload"] ?: map["payload"])?.let { put("payload", it) }
            (map["candidateConceptLinks"] as? List<*>)?.takeIf { it.isNotEmpty() }?.let {
                put("candidateConceptLinks", it)
            }
        }
    }

    /**
     * @param kind persisted artefact kind
     * @param artifactType optional inner `artifactType` field
     * @return true when the row is a schema authoring capture
     */
    fun isSchemaAuthoringPersistKind(kind: String, artifactType: String?): Boolean =
        kind.contains("schema.authoring", ignoreCase = true) ||
            artifactType == "schema-capture"
}
