package io.qpointz.mill.metadata.service

/**
 * Normalises arbitrary JSON facet bodies to the map shape expected by [FacetService.assign].
 */
object FacetPayloadCoercion {

    /**
     * @param payload raw JSON value from a REST body (object, array, scalar, or null)
     * @return map suitable for persistence; scalars are wrapped as {@code {"value": …}}
     */
    @Suppress("UNCHECKED_CAST")
    fun toPayloadMap(payload: Any?): Map<String, Any?> = when (payload) {
        null -> emptyMap()
        is Map<*, *> -> payload.entries.associate { (k, v) -> k.toString() to v }
        is List<*> ->
            if (payload.size == 1 && payload[0] is Map<*, *>) {
                (payload[0] as Map<*, *>).entries.associate { (k, v) -> k.toString() to v }
            } else {
                mapOf("value" to payload)
            }
        else -> mapOf("value" to payload)
    }
}
