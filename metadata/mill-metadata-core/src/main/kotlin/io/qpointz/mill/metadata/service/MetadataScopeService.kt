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

    /**
     * Idempotently creates or updates the chat metadata scope row for [chatId].
     *
     * @param chatId conversation GUID
     * @param chatTitle persisted chat title (without `Chat ` prefix)
     * @param ownerId chat owner user id
     */
    fun ensureChatScope(chatId: String, chatTitle: String?, ownerId: String?): MetadataScope {
        val res = MetadataEntityUrn.canonicalize(MetadataUrns.scopeChat(chatId))
        val displayName = chatDisplayName(chatTitle)
        val existing = repo.findByRes(res)
        val now = Instant.now()
        if (existing != null) {
            if (existing.displayName == displayName && existing.ownerId == ownerId) {
                return existing
            }
            return repo.save(
                existing.copy(
                    displayName = displayName,
                    ownerId = ownerId ?: existing.ownerId,
                    lastModifiedAt = now,
                ),
            )
        }
        return repo.save(
            MetadataScope(
                res = res,
                scopeType = "CHAT",
                referenceId = chatId,
                displayName = displayName,
                ownerId = ownerId,
                visibility = "PRIVATE",
                uuid = UUID.randomUUID().toString(),
                createdAt = now,
                createdBy = null,
                lastModifiedAt = now,
                lastModifiedBy = null,
            ),
        )
    }

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

    private fun chatDisplayName(chatTitle: String?): String {
        val title = chatTitle?.trim()?.takeIf { it.isNotEmpty() }
        return if (title != null) "Chat $title" else "Chat"
    }
}
