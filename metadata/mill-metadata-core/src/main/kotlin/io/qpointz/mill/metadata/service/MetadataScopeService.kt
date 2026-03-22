package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import java.time.Instant
import java.util.Optional

/**
 * Service facade for [MetadataScope] lifecycle management.
 *
 * Scopes are analogous to branches in version control — each is a named, independent set of
 * facet data with no platform-defined precedence between them. The global scope is the only
 * guaranteed-present scope and cannot be deleted.
 *
 * Context composition (which scopes to merge and in what order) is the caller's responsibility
 * and is expressed through [MetadataContext]; no precedence logic lives here.
 *
 * @param repo the [MetadataScopeRepository] backing this service
 */
class MetadataScopeService(private val repo: MetadataScopeRepository) {

    /**
     * Returns the global scope, which is always present after Flyway V4.
     *
     * @return the global [MetadataScope]
     * @throws IllegalStateException if the global scope is absent (Flyway migration may not have run)
     */
    fun globalScope(): MetadataScope = repo.findById(MetadataUrns.SCOPE_GLOBAL)
        .orElseThrow { IllegalStateException("Global scope not found — Flyway migration may not have run") }

    /**
     * Returns all known scopes.
     *
     * @return list of all registered scopes
     */
    fun findAll(): List<MetadataScope> = repo.findAll()

    /**
     * Returns the scope for the given URN key, or empty if it does not exist.
     *
     * @param scopeKey full Mill scope URN key
     * @return an [Optional] containing the scope, or empty if not found
     */
    fun findByKey(scopeKey: String): Optional<MetadataScope> = repo.findById(scopeKey)

    /**
     * Creates a new scope. Throws if a scope with the same URN key already exists.
     *
     * @param scopeId     full Mill scope URN key for the new scope
     * @param displayName optional human-readable label
     * @param ownerId     optional owner identifier; null for shared or impersonal scopes
     * @return the persisted [MetadataScope]
     * @throws IllegalArgumentException if a scope with [scopeId] already exists
     */
    fun create(scopeId: String, displayName: String?, ownerId: String?): MetadataScope {
        require(!repo.existsById(scopeId)) { "Scope already exists: $scopeId" }
        return repo.save(MetadataScope(scopeId, displayName, ownerId, Instant.now()))
    }

    /**
     * Deletes the scope with the given URN key.
     *
     * The global scope (`urn:mill/metadata/scope:global`) cannot be deleted.
     *
     * @param scopeId full Mill scope URN key to delete
     * @throws IllegalArgumentException if [scopeId] is the global scope
     */
    fun delete(scopeId: String) {
        require(scopeId != MetadataUrns.SCOPE_GLOBAL) { "Cannot delete the global scope" }
        repo.deleteById(scopeId)
    }
}
