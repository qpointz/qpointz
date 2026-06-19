package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.memory.ConversationMemory
import io.qpointz.mill.ai.persistence.ActiveArtifactPointer
import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.persistence.ConversationTurn
import io.qpointz.mill.ai.persistence.RunEventRecord
import io.qpointz.mill.ai.runtime.ConversationMessage
import io.qpointz.mill.ai.runtime.MessageRole
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaActiveArtifactPointerStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaArtifactStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatMemoryStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatRegistry
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaConversationStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaRunEventStore
import io.qpointz.mill.persistence.ai.jpa.repositories.ActiveArtifactPointerRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.AiRelationRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ArtifactRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatMemoryMessageRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatMemoryRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatTurnRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.RunEventRepository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Verifies WI-324: deleting [io.qpointz.mill.ai.persistence.ChatRegistry] row cascades to
 * all `ai_chat_*` satellite tables.
 */
@SpringBootTest
@Transactional
class JpaChatDeleteCascadeIT {

    @Autowired lateinit var chatRepo: ChatRepository
    @Autowired lateinit var turnRepo: ChatTurnRepository
    @Autowired lateinit var memoryRepo: ChatMemoryRepository
    @Autowired lateinit var messageRepo: ChatMemoryMessageRepository
    @Autowired lateinit var artifactRepo: ArtifactRepository
    @Autowired lateinit var pointerRepo: ActiveArtifactPointerRepository
    @Autowired lateinit var runEventRepo: RunEventRepository
    @Autowired lateinit var relationRepo: AiRelationRepository
    @Autowired lateinit var entityManager: EntityManager

    private val registry by lazy { JpaChatRegistry(chatRepo) }
    private val conversationStore by lazy {
        JpaConversationStore(chatRepo, turnRepo, relationRepo, artifactRepo)
    }
    private val memoryStore by lazy { JpaChatMemoryStore(memoryRepo, messageRepo) }
    private val artifactStore by lazy { JpaArtifactStore(artifactRepo) }
    private val pointerStore by lazy { JpaActiveArtifactPointerStore(pointerRepo) }
    private val runEventStore by lazy { JpaRunEventStore(runEventRepo) }

    @Test
    fun `delete chat cascades to turns memory artifacts pointers and run events`() {
        val chatId = "cascade-chat-1"
        val now = Instant.now()
        registry.create(
            ChatMetadata(
                chatId = chatId,
                userId = "user-1",
                profileId = "hello-world",
                chatName = "Cascade test",
                chatType = "general",
                createdAt = now,
                updatedAt = now,
            ),
        )

        conversationStore.appendTurn(
            chatId,
            ConversationTurn(
                turnId = "turn-1",
                role = "user",
                text = "hello",
                profileId = "hello-world",
                createdAt = now,
            ),
        )

        memoryStore.save(
            ConversationMemory(
                conversationId = chatId,
                profileId = "hello-world",
                messages = listOf(ConversationMessage(MessageRole.USER, "hi")),
            ),
        )

        val artifactId = "art-1"
        artifactStore.save(
            ArtifactRecord(
                artifactId = artifactId,
                conversationId = chatId,
                runId = "run-1",
                kind = "sql.generated",
                payload = mapOf("sql" to "SELECT 1"),
                createdAt = now,
            ),
        )
        pointerStore.upsert(ActiveArtifactPointer(chatId, "last-sql", artifactId, now))

        runEventStore.save(
            RunEventRecord(
                eventId = UUID.randomUUID().toString(),
                runId = "run-1",
                conversationId = chatId,
                profileId = "hello-world",
                kind = "tool.result",
                runtimeType = "ToolResult",
                content = mapOf("ok" to true),
                createdAt = now,
            ),
        )

        assertThat(turnRepo.countByChatId(chatId)).isEqualTo(1)
        assertThat(memoryRepo.findById(chatId)).isPresent
        assertThat(artifactRepo.findByChatIdOrderByCreatedAtAsc(chatId)).hasSize(1)
        assertThat(pointerRepo.findByIdChatId(chatId)).hasSize(1)
        assertThat(runEventRepo.countByChatId(chatId)).isEqualTo(1)

        assertThat(registry.delete(chatId)).isTrue()
        entityManager.flush()
        entityManager.clear()

        assertThat(chatRepo.findById(chatId)).isEmpty
        assertThat(turnRepo.countByChatId(chatId)).isZero()
        assertThat(memoryRepo.findById(chatId)).isEmpty
        assertThat(messageRepo.findByChatIdOrderByPositionAsc(chatId)).isEmpty()
        assertThat(artifactRepo.findByChatIdOrderByCreatedAtAsc(chatId)).isEmpty()
        assertThat(pointerRepo.findByIdChatId(chatId)).isEmpty()
        assertThat(runEventRepo.countByChatId(chatId)).isZero()
    }
}
