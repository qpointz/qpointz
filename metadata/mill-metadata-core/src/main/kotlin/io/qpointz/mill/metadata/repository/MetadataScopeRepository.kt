package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataScope

/** Persistence for `metadata_scope` — SPEC §6.4. */
interface MetadataScopeRepository {
    fun findByRes(scopeRes: String): MetadataScope?

    fun findAll(): List<MetadataScope>

    fun findByType(scopeType: String): List<MetadataScope>

    fun exists(scopeRes: String): Boolean

    fun save(scope: MetadataScope): MetadataScope

    fun delete(scopeRes: String)
}
