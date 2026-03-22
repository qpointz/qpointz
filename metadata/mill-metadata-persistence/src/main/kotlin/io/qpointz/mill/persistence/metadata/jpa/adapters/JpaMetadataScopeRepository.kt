package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataScopeEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataScopeJpaRepository
import org.slf4j.LoggerFactory
import java.util.Optional

/**
 * [MetadataScopeRepository] adapter backed by [MetadataScopeJpaRepository].
 *
 * Maps between the pure domain [MetadataScope] type and the JPA entity [MetadataScopeEntity].
 * Persistence-only fields (`scopeType`, `referenceId`, `visibility`) are inferred from the
 * scope URN on write and are not exposed on the domain type.
 *
 * The `scopeType` column is derived from the local part of the scope URN:
 * - `urn:mill/metadata/scope:global` → `GLOBAL` / `referenceId = null`
 * - `urn:mill/metadata/scope:user:<id>` → `USER` / `referenceId = <id>`
 * - `urn:mill/metadata/scope:team:<name>` → `TEAM` / `referenceId = <name>`
 * - `urn:mill/metadata/scope:role:<name>` → `ROLE` / `referenceId = <name>`
 * - all other patterns → `CUSTOM` / `referenceId = local part`
 *
 * @param jpaRepo the Spring Data JPA repository for `metadata_scope`
 */
class JpaMetadataScopeRepository(
    private val jpaRepo: MetadataScopeJpaRepository
) : MetadataScopeRepository {

    /**
     * Finds a scope by its URN identifier.
     *
     * @param scopeId full Mill scope URN key
     * @return an [Optional] containing the domain scope, or empty if not found
     */
    override fun findById(scopeId: String): Optional<MetadataScope> =
        jpaRepo.findById(scopeId).map { toDomain(it) }

    /**
     * Returns all registered scopes.
     *
     * @return list of all scopes as domain objects
     */
    override fun findAll(): List<MetadataScope> =
        jpaRepo.findAll().map { toDomain(it) }

    /**
     * Saves (inserts or updates) the given scope.
     *
     * Scope type and reference ID are inferred from the [MetadataScope.scopeId] URN.
     *
     * @param scope the scope to persist
     * @return the saved domain scope
     */
    override fun save(scope: MetadataScope): MetadataScope {
        val entity = toEntity(scope)
        val saved = jpaRepo.save(entity)
        log.info("Saved scope: {}", saved.scopeId)
        return toDomain(saved)
    }

    /**
     * Deletes the scope with the given URN identifier.
     *
     * No-op if the scope does not exist.
     *
     * @param scopeId full Mill scope URN key to delete
     */
    override fun deleteById(scopeId: String) {
        jpaRepo.deleteById(scopeId)
        log.info("Deleted scope: {}", scopeId)
    }

    /**
     * Returns `true` if a scope with the given URN identifier exists.
     *
     * @param scopeId full Mill scope URN key to check
     * @return `true` if the scope is present
     */
    override fun existsById(scopeId: String): Boolean = jpaRepo.existsById(scopeId)

    /**
     * Maps a [MetadataScopeEntity] to its domain [MetadataScope] representation.
     *
     * @param entity the JPA entity to convert
     * @return the corresponding domain [MetadataScope]
     */
    internal fun toDomain(entity: MetadataScopeEntity): MetadataScope =
        MetadataScope(
            scopeId = entity.scopeId,
            displayName = entity.displayName,
            ownerId = entity.ownerId,
            createdAt = entity.createdAt
        )

    /**
     * Maps a domain [MetadataScope] to its JPA entity representation.
     *
     * Infers `scopeType` and `referenceId` from the scope URN local part.
     *
     * @param scope the domain scope to convert
     * @return the corresponding [MetadataScopeEntity]
     */
    internal fun toEntity(scope: MetadataScope): MetadataScopeEntity {
        val (scopeType, referenceId) = parseScopeKey(scope.scopeId)
        return MetadataScopeEntity(
            scopeId = scope.scopeId,
            scopeType = scopeType,
            referenceId = referenceId,
            displayName = scope.displayName,
            ownerId = scope.ownerId,
            visibility = "PUBLIC",
            createdAt = scope.createdAt
        )
    }

    /**
     * Parses a scope URN into a `(scopeType, referenceId)` pair.
     *
     * @param scopeId full Mill scope URN key
     * @return pair of scope type string and optional reference identifier
     */
    private fun parseScopeKey(scopeId: String): Pair<String, String?> {
        if (scopeId == MetadataUrns.SCOPE_GLOBAL) return Pair("GLOBAL", null)
        val local = scopeId.removePrefix(MetadataUrns.SCOPE_PREFIX)
        return when {
            local.startsWith("user:") -> Pair("USER", local.removePrefix("user:"))
            local.startsWith("team:") -> Pair("TEAM", local.removePrefix("team:"))
            local.startsWith("role:") -> Pair("ROLE", local.removePrefix("role:"))
            else                      -> Pair("CUSTOM", local)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JpaMetadataScopeRepository::class.java)
    }
}
