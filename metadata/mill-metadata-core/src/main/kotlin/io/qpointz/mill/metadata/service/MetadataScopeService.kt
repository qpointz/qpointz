package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Scope lifecycle facade (SPEC §7.4 patterns).
 *
 * @param repo scope persistence
 */
class MetadataScopeService(private val repo: MetadataScopeRepository) {

    fun globalScope(): MetadataScope {
        val g = MetadataEntityUrn.canonicalize(MetadataUrns.SCOPE_GLOBAL)
        return repo.findByRes(g) ?: error("Global scope not found — Flyway migration may not have run")
    }

    fun findAll(): List<MetadataScope> = repo.findAll()

    fun findByKey(scopeKey: String): Optional<MetadataScope> =
        Optional.ofNullable(repo.findByRes(MetadataEntityUrn.canonicalize(scopeKey)))

    fun create(scopeId: String, displayName: String?, ownerId: String?): MetadataScope {
        val res = MetadataEntityUrn.canonicalize(scopeId)
        require(!repo.exists(res)) { "Scope already exists: $res" }
        val now = Instant.now()
        val scope = MetadataScope(
            res = res,
            scopeType = "CUSTOM",
            referenceId = null,
            displayName = displayName,
            ownerId = ownerId,
            visibility = "PUBLIC",
            uuid = UUID.randomUUID().toString(),
            createdAt = now,
            createdBy = null,
            lastModifiedAt = now,
            lastModifiedBy = null
        )
        return repo.save(scope)
    }

    fun delete(scopeId: String) {
        val res = MetadataEntityUrn.canonicalize(scopeId)
        require(res != MetadataEntityUrn.canonicalize(MetadataUrns.SCOPE_GLOBAL)) {
            "Cannot delete the global scope"
        }
        repo.delete(res)
    }
}
