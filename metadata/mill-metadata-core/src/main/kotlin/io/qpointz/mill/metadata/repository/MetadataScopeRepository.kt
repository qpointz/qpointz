package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataScope
import java.util.Optional

/**
 * Persistence contract for [MetadataScope] lifecycle management.
 *
 * Scope identifiers use full Mill URN notation, e.g. `"urn:mill/metadata/scope:global"`.
 * Implementations are responsible for mapping between the domain [MetadataScope] type and
 * their underlying storage format without exposing persistence annotations on this interface.
 */
interface MetadataScopeRepository {

    /**
     * Finds a scope by its URN identifier.
     *
     * @param scopeId full Mill scope URN key
     * @return an [Optional] containing the scope, or empty if not found
     */
    fun findById(scopeId: String): Optional<MetadataScope>

    /**
     * Returns all registered scopes.
     *
     * @return list of all known scopes
     */
    fun findAll(): List<MetadataScope>

    /**
     * Saves (inserts or updates) the given scope.
     *
     * @param scope the scope to persist
     * @return the persisted scope (may differ from input if the implementation adds defaults)
     */
    fun save(scope: MetadataScope): MetadataScope

    /**
     * Deletes the scope with the given URN identifier.
     *
     * No-op if the scope does not exist.
     *
     * @param scopeId full Mill scope URN key to delete
     */
    fun deleteById(scopeId: String)

    /**
     * Returns `true` if a scope with the given URN identifier exists.
     *
     * @param scopeId full Mill scope URN key to check
     * @return `true` if the scope is registered
     */
    fun existsById(scopeId: String): Boolean
}
