package io.qpointz.mill.metadata.domain

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.util.Optional

/** Converts raw facet payloads to typed facet classes through Jackson mapping. */
class FacetConverter(private val objectMapper: ObjectMapper) {

    /** Converts arbitrary raw value to the requested facet class when possible. */
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
        private val DEFAULT = FacetConverter(createDefaultMapper())

        /** Returns shared converter configured with metadata defaults. */
        @JvmStatic
        fun defaultConverter(): FacetConverter = DEFAULT

        private fun createDefaultMapper(): ObjectMapper =
            ObjectMapper().apply {
                registerModule(JavaTimeModule())
                registerModule(Jdk8Module())
                registerKotlinModule()
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
    }
}
