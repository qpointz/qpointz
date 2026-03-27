package io.qpointz.mill.metadata.service

/**
 * Helpers for MULTIPLE-cardinality facet payloads stored as a JSON array or a relation-style
 * envelope `{ "relations": [ ... ] }`, aligned with the mill-ui [EntityDetails] helpers.
 */
internal object FacetPayloadMultiple {

    /**
     * @return logical facet instances in display/API order; empty when there are no instances
     */
    fun itemValues(payload: Any?): List<Any?> {
        if (payload == null) return emptyList()
        if (payload is List<*>) return payload
        if (payload is Map<*, *>) {
            val rel = payload["relations"]
            if (rel is List<*>) return rel
            if (payload.isEmpty()) return emptyList()
            return listOf(payload)
        }
        return listOf(payload)
    }

    /**
     * Rebuilds the stored payload after removing or splicing instances, preserving non-`relations`
     * keys on envelope maps.
     */
    fun rebuild(original: Any?, newItems: List<Any?>): Any? {
        when (original) {
            is List<*> -> return ArrayList(newItems)
            is Map<*, *> -> {
                val rel = original["relations"]
                if (rel is List<*>) {
                    val out = LinkedHashMap<String, Any?>()
                    for ((k, v) in original) {
                        val key = k?.toString() ?: continue
                        if (key == "relations") continue
                        @Suppress("UNCHECKED_CAST")
                        out[key] = v as Any?
                    }
                    out["relations"] = ArrayList(newItems)
                    return out
                }
            }
        }
        return ArrayList(newItems)
    }
}
