package io.qpointz.mill.persistence.ai.jpa.adapters

import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ChatRegistry
import io.qpointz.mill.ai.persistence.ChatUpdate
import io.qpointz.mill.persistence.ai.jpa.entities.ChatMetadataEntity
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatMetadataRepository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

open class JpaChatRegistry(
    private val repo: ChatMetadataRepository,
) : ChatRegistry {

    @Transactional
    override fun create(metadata: ChatMetadata): ChatMetadata {
        repo.save(metadata.toEntity())
        return metadata
    }

    override fun load(chatId: String): ChatMetadata? =
        repo.findById(chatId).orElse(null)?.toDomain()

    override fun list(userId: String): List<ChatMetadata> =
        repo.findByUserIdOrderByCreatedAtDesc(userId).map { it.toDomain() }

    @Transactional
    override fun update(chatId: String, update: ChatUpdate): ChatMetadata? {
        val entity = repo.findById(chatId).orElse(null) ?: return null
        val now = Instant.now()
        // Immutable fields are taken from the loaded entity — never from the update.
        val updated = entity.toDomain().copy(
            chatName = update.chatName ?: entity.chatName,
            isFavorite = update.isFavorite ?: entity.isFavorite,
            contextLabel = update.contextLabel ?: entity.contextLabel,
            updatedAt = now,
        )
        repo.save(updated.toEntity())
        return updated
    }

    @Transactional
    override fun delete(chatId: String): Boolean {
        if (!repo.existsById(chatId)) return false
        repo.deleteById(chatId)
        return true
    }

    override fun findByContext(userId: String, contextType: String, contextId: String): ChatMetadata? =
        repo.findByUserIdAndContextTypeAndContextId(userId, contextType, contextId)?.toDomain()

    private fun ChatMetadata.toEntity() = ChatMetadataEntity(
        chatId = chatId,
        userId = userId,
        profileId = profileId,
        chatName = chatName,
        chatType = chatType,
        isFavorite = isFavorite,
        contextType = contextType,
        contextId = contextId,
        contextLabel = contextLabel,
        contextEntityType = contextEntityType,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun ChatMetadataEntity.toDomain() = ChatMetadata(
        chatId = chatId,
        userId = userId,
        profileId = profileId,
        chatName = chatName,
        chatType = chatType,
        isFavorite = isFavorite,
        contextType = contextType,
        contextId = contextId,
        contextLabel = contextLabel,
        contextEntityType = contextEntityType,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
