package io.qpointz.mill.metadata.domain

import io.qpointz.mill.utils.JsonUtils
import java.util.Optional

/** Converts raw facet payloads to typed facet classes using the shared Mill JSON mapper. */
class FacetConverter {

    private val objectMapper = JsonUtils.defaultJsonMapper()

    /** @param raw payload or already-converted instance */
    fun <T : Any> convert(raw: Any?, facetClass: Class<T>): Optional<T> {
        if (raw == null) return Optional.empty()
        if (facetClass.isInstance(raw)) return Optional.of(facetClass.cast(raw))
        return try {
            Optional.ofNullable(objectMapper.convertValue(raw, facetClass))
        } catch (_: Exception) {
            Optional.empty()
        }
    }

    companion object {
        private val DEFAULT = FacetConverter()

        @JvmStatic
        fun defaultConverter(): FacetConverter = DEFAULT
    }
}
