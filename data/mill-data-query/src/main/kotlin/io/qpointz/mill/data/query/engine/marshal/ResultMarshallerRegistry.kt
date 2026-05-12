package io.qpointz.mill.data.query.engine.marshal

import java.util.Locale

/**
 * Registry of [ResultMarshaller] instances keyed by [ResultMarshaller.formatId].
 *
 * @param byId map of format id to marshaller (case-insensitive keys normalized to lower case).
 */
class ResultMarshallerRegistry(
    private val byId: Map<String, ResultMarshaller>,
) {
    /**
     * @param formatId requested format id (case-insensitive).
     * @return marshaller or null if unknown.
     */
    fun byFormatId(formatId: String): ResultMarshaller? = byId[formatId.lowercase(Locale.ROOT)]

    /**
     * @return all registered marshallers (immutable).
     */
    fun all(): Collection<ResultMarshaller> = byId.values

    companion object {
        /**
         * Loads providers via [ServiceLoader], merges marshallers, and fails fast on duplicate ids.
         *
         * @param classLoader class loader used for service discovery (application class loader in Spring).
         */
        @JvmStatic
        fun load(classLoader: ClassLoader = Thread.currentThread().contextClassLoader): ResultMarshallerRegistry {
            val merged = LinkedHashMap<String, ResultMarshaller>()
            val loader = java.util.ServiceLoader.load(ResultMarshallerProvider::class.java, classLoader)
            for (provider in loader) {
                for (m in provider.marshallers()) {
                    val key = m.formatId.lowercase(Locale.ROOT)
                    require(!merged.containsKey(key)) {
                        "Duplicate ResultMarshaller format id '${m.formatId}'"
                    }
                    merged[key] = m
                }
            }
            return ResultMarshallerRegistry(merged.toMap())
        }
    }
}
