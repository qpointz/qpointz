package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

/** Thread-safe in-memory implementation of [FacetTypeRepository]. */
class InMemoryFacetTypeRepository : FacetTypeRepository {

    private val store = ConcurrentHashMap<String, FacetTypeDescriptor>()

    override fun save(descriptor: FacetTypeDescriptor) {
        store[descriptor.typeKey] = descriptor
    }

    override fun findByTypeKey(typeKey: String): Optional<FacetTypeDescriptor> =
        Optional.ofNullable(store[typeKey])

    override fun findAll(): Collection<FacetTypeDescriptor> =
        store.values.toList()

    override fun deleteByTypeKey(typeKey: String) {
        store.remove(typeKey)
    }

    override fun existsByTypeKey(typeKey: String): Boolean =
        store.containsKey(typeKey)
}
