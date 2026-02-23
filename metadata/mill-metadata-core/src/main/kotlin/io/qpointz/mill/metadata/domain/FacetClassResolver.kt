package io.qpointz.mill.metadata.domain

import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

/** Resolves facet type keys to concrete facet implementation classes. */
interface FacetClassResolver {
    /** Returns the mapped class for a type key, if registered. */
    fun resolve(typeKey: String): Optional<Class<out MetadataFacet>>
}

/** In-memory resolver with runtime registration support. */
class DefaultFacetClassResolver(initial: Map<String, Class<out MetadataFacet>>? = null) : FacetClassResolver {

    private val mappings = ConcurrentHashMap<String, Class<out MetadataFacet>>()

    init {
        initial?.let { mappings.putAll(it) }
    }

    /** Registers or replaces mapping for a facet type key. */
    fun register(typeKey: String, facetClass: Class<out MetadataFacet>) {
        mappings[typeKey] = facetClass
    }

    override fun resolve(typeKey: String): Optional<Class<out MetadataFacet>> =
        Optional.ofNullable(mappings[typeKey])
}
