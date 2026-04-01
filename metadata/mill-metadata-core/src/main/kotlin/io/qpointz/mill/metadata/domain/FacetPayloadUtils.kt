package io.qpointz.mill.metadata.domain

import io.qpointz.mill.utils.JsonUtils
import java.util.Optional

/**
 * Jackson-based conversion from raw facet payloads ([Map], existing instances, etc.) to typed
 * facet data classes. Replaces the former Jackson-only converter bridge without a resolver/registry.
 */
object FacetPayloadUtils {

    private val objectMapper = JsonUtils.defaultJsonMapper()

    /**
     * Converts [raw] to [clazz] using the shared Mill JSON mapper.
     *
     * @param raw payload map, or an instance already of [clazz]
     * @param clazz target facet type
     * @return present value when conversion succeeds
     */
    @JvmStatic
    fun <T : Any> convert(raw: Any?, clazz: Class<T>): Optional<T> {
        if (raw == null) return Optional.empty()
        if (clazz.isInstance(raw)) return Optional.of(clazz.cast(raw))
        return try {
            Optional.ofNullable(objectMapper.convertValue(raw, clazz))
        } catch (_: Exception) {
            Optional.empty()
        }
    }
}
