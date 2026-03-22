package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataScope
import java.util.Optional

/**
 * No-operation [MetadataScopeRepository] that simulates an absent scope store.
 *
 * All read operations return empty results; write and delete operations are silent no-ops.
 * [save] returns the input scope unchanged so callers can chain the result without null checks.
 * Used as the autoconfiguration fallback when no storage backend is configured.
 */
object NoOpMetadataScopeRepository : MetadataScopeRepository {

    /** Always returns [Optional.empty]: no scopes are stored. */
    override fun findById(scopeId: String): Optional<MetadataScope> = Optional.empty()

    /** Always returns an empty list: no scopes are stored. */
    override fun findAll(): List<MetadataScope> = emptyList()

    /** No-op: returns [scope] unchanged. */
    override fun save(scope: MetadataScope): MetadataScope = scope

    /** No-op: nothing to delete. */
    override fun deleteById(scopeId: String) = Unit

    /** Always returns `false`: no scopes are stored. */
    override fun existsById(scopeId: String): Boolean = false
}
