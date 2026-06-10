package io.qpointz.mill.persistence.ai.jpa.adapters

import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.memory.ConversationMemory
import io.qpointz.mill.ai.runtime.ConversationMessage
import io.qpointz.mill.persistence.ai.jpa.entities.ChatMemoryEntity
import io.qpointz.mill.persistence.ai.jpa.entities.ChatMemoryMessageEntity
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatMemoryMessageRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatMemoryRepository
import io.qpointz.mill.ai.runtime.MessageRole
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

open class JpaChatMemoryStore(
    private val memoryRepo: ChatMemoryRepository,
    private val messageRepo: ChatMemoryMessageRepository,
) : ChatMemoryStore {

    override fun load(conversationId: String): ConversationMemory? {
        val entity = memoryRepo.findById(conversationId).orElse(null) ?: return null
        val messages = messageRepo.findByConversationIdOrderByPositionAsc(conversationId)
            .map { it.toDomain() }
        return ConversationMemory(
            conversationId = entity.conversationId,
            profileId = entity.profileId,
            messages = messages,
        )
    }

    @Transactional
    override fun save(memory: ConversationMemory) {
        val now = Instant.now()
        val existing = memoryRepo.findById(memory.conversationId).orElse(null)
        if (existing == null) {
            memoryRepo.save(
                ChatMemoryEntity(
                    conversationId = memory.conversationId,
                    profileId = memory.profileId,
                    updatedAt = now,
                )
            )
        } else {
            memoryRepo.save(
                ChatMemoryEntity(
                    conversationId = existing.conversationId,
                    profileId = existing.profileId,
                    updatedAt = now,
                )
            )
        }
        // Replace all messages transactionally
        messageRepo.deleteByConversationId(memory.conversationId)
        memory.messages.forEachIndexed { idx, msg ->
            messageRepo.save(msg.toEntity(memory.conversationId, idx))
        }
    }

    @Transactional
    override fun clear(conversationId: String) {
        memoryRepo.deleteById(conversationId)
    }

    private fun ChatMemoryMessageEntity.toDomain(): ConversationMessage =
        ConversationMessage(
            role = MessageRole.valueOf(role.uppercase()),
            content = content,
            toolCallId = toolCallId,
            toolName = toolName,
        )

    private fun ConversationMessage.toEntity(conversationId: String, position: Int): ChatMemoryMessageEntity =
        ChatMemoryMessageEntity(
            conversationId = conversationId,
            position = position,
            role = role.name.lowercase(),
            content = content,
            toolCallId = toolCallId,
            toolName = toolName,
        )
}
