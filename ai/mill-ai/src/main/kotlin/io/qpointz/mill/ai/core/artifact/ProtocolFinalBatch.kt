package io.qpointz.mill.ai.core.artifact

/**
 * Expands and collapses batch `ProtocolFinal` payloads (`{ results: [...] }`).
 *
 * Scalar (legacy) capture maps are treated as a single-item batch for fan-out.
 */
object ProtocolFinalBatch {

    /** JSON field name for batch protocol finals. */
    const val RESULTS_FIELD: String = "results"

    /**
     * @param payload protocol final body
     * @return true when [payload] is an explicit `{ results: [...] }` envelope
     */
    @Suppress("UNCHECKED_CAST")
    fun isBatchEnvelope(payload: Any?): Boolean {
        val map = payload as? Map<String, Any?> ?: return false
        return map[RESULTS_FIELD] is List<*>
    }

    /**
     * Splits a protocol final payload into per-artefact item maps.
     *
     * @param payload scalar capture map or batch envelope
     * @return one or more item payloads (never empty when [payload] is non-null map)
     */
    @Suppress("UNCHECKED_CAST")
    fun expandItemPayloads(payload: Any?): List<Map<String, Any?>> {
        val map = payload as? Map<String, Any?> ?: return emptyList()
        val results = map[RESULTS_FIELD] as? List<*>
        if (results != null) {
            return results.mapNotNull { it as? Map<String, Any?> }
        }
        return listOf(map)
    }

    /**
     * Wraps successful capture item maps in a batch envelope.
     *
     * @param items per-capture payload maps
     * @param forceBatch when true, always emit `{ results: [...] }` even for a single item
     */
    fun collapseResults(items: List<Map<String, Any?>>, forceBatch: Boolean = false): Map<String, Any?> =
        when {
            items.isEmpty() -> emptyMap()
            items.size == 1 && !forceBatch -> items.single()
            else -> mapOf(RESULTS_FIELD to items)
        }
}
