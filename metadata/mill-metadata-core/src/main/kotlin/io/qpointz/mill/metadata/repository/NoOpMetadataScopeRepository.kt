package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataScope

/** No-op [MetadataScopeRepository]. */
class NoOpMetadataScopeRepository : MetadataScopeRepository {
    override fun findByRes(scopeRes: String): MetadataScope? = null
    override fun findAll(): List<MetadataScope> = emptyList()
    override fun findByType(scopeType: String): List<MetadataScope> = emptyList()
    override fun exists(scopeRes: String): Boolean = false
    override fun save(scope: MetadataScope): MetadataScope = scope
    override fun delete(scopeRes: String) = Unit
}
