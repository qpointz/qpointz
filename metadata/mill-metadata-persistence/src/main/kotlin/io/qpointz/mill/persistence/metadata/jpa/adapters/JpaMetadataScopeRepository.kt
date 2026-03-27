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
 * The domain [MetadataScope.scopeId] field holds the scope URN and maps to [MetadataScopeEntity.scopeRes].
 */
class JpaMetadataScopeRepository(
    private val jpaRepo: MetadataScopeJpaRepository
) : MetadataScopeRepository {

    override fun findById(scopeId: String): Optional<MetadataScope> =
        jpaRepo.findByScopeRes(scopeId).map { toDomain(it) }

    override fun findAll(): List<MetadataScope> =
        jpaRepo.findAll().map { toDomain(it) }

    override fun save(scope: MetadataScope): MetadataScope {
        val (scopeType, referenceId) = parseScopeKey(scope.scopeId)
        val entity = jpaRepo.findByScopeRes(scope.scopeId)
            .map { existing ->
                existing.scopeType = scopeType
                existing.referenceId = referenceId
                existing.displayName = scope.displayName
                existing.ownerId = scope.ownerId
                existing
            }.orElse(
                MetadataScopeEntity(
                    scopeId = 0L,
                    scopeRes = scope.scopeId,
                    scopeType = scopeType,
                    referenceId = referenceId,
                    displayName = scope.displayName,
                    ownerId = scope.ownerId,
                    visibility = "PUBLIC",
                    createdAt = scope.createdAt
                )
            )
        val saved = jpaRepo.save(entity)
        log.info("Saved scope: {}", saved.scopeRes)
        return toDomain(saved)
    }

    override fun deleteById(scopeId: String) {
        jpaRepo.findByScopeRes(scopeId).ifPresent { jpaRepo.delete(it) }
        log.info("Deleted scope: {}", scopeId)
    }

    override fun existsById(scopeId: String): Boolean =
        jpaRepo.findByScopeRes(scopeId).isPresent

    internal fun toDomain(entity: MetadataScopeEntity): MetadataScope =
        MetadataScope(
            scopeId = entity.scopeRes,
            displayName = entity.displayName,
            ownerId = entity.ownerId,
            createdAt = entity.createdAt
        )

    private fun parseScopeKey(scopeId: String): Pair<String, String?> {
        if (scopeId == MetadataUrns.SCOPE_GLOBAL) return Pair("GLOBAL", null)
        val local = scopeId.removePrefix(MetadataUrns.SCOPE_PREFIX)
        return when {
            local.startsWith("user:") -> Pair("USER", local.removePrefix("user:"))
            local.startsWith("team:") -> Pair("TEAM", local.removePrefix("team:"))
            local.startsWith("role:") -> Pair("ROLE", local.removePrefix("role:"))
            else -> Pair("CUSTOM", local)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JpaMetadataScopeRepository::class.java)
    }
}
