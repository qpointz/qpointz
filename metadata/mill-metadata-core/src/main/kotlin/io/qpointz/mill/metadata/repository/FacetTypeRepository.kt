package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor
import java.util.Optional

/** Persistence abstraction for facet type descriptors (catalog metadata). */
interface FacetTypeRepository {
    fun save(descriptor: FacetTypeDescriptor)
    fun findByTypeKey(typeKey: String): Optional<FacetTypeDescriptor>
    fun findAll(): Collection<FacetTypeDescriptor>
    fun deleteByTypeKey(typeKey: String)
    fun existsByTypeKey(typeKey: String): Boolean

    /**
     * Returns how many persisted facet payload rows reference the given facet type.
     *
     * Implementations that do not have access to entity facet storage may return `0`.
     */
    fun usageCount(typeKey: String): Long
}
