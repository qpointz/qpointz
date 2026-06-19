package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.persistence.ConversationTurn
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaArtifactStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatRegistry
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaConversationStore
import io.qpointz.mill.persistence.ai.jpa.entities.ChatEntity
import io.qpointz.mill.persistence.ai.jpa.repositories.AiRelationRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ArtifactRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ChatTurnRepository
import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.persistence.ChatMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
class JpaConversationStoreIT {

    @Autowired lateinit var chatRepo: ChatRepository
    @Autowired lateinit var turnRepo: ChatTurnRepository
    @Autowired lateinit var relationRepo: AiRelationRepository
    @Autowired lateinit var artifactRepo: ArtifactRepository

    private val registry by lazy { JpaChatRegistry(chatRepo) }
    private val store by lazy { JpaConversationStore(chatRepo, turnRepo, relationRepo, artifactRepo) }
    private val artifactStore by lazy { JpaArtifactStore(artifactRepo) }

    private fun seedChat(
        chatId: String,
        profileId: String = "p",
        userId: String = "user-1",
    ) {
        registry.create(
            ChatMetadata(
                chatId = chatId,
                userId = userId,
                profileId = profileId,
                chatName = "Test",
                chatType = "general",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            ),
        )
    }

    @Test
    fun `ensureExists touches existing chat row`() {
        seedChat("c1", "profile-a")
        store.ensureExists("c1", "profile-a")
        assertThat(chatRepo.findById("c1")).isPresent
    }

    @Test
    fun `appendTurn assigns position in order`() {
        seedChat("c2")
        store.appendTurn("c2", ConversationTurn("t1", "user", "hello", profileId = "p", createdAt = Instant.now()))
        store.appendTurn("c2", ConversationTurn("t2", "assistant", "hi", profileId = "p", createdAt = Instant.now()))
        val record = store.load("c2")!!
        assertThat(record.turns).hasSize(2)
        assertThat(record.turns[0].turnId).isEqualTo("t1")
        assertThat(record.turns[1].turnId).isEqualTo("t2")
    }

    @Test
    fun `attachArtifacts links artifacts to a turn`() {
        seedChat("c3")
        store.appendTurn("c3", ConversationTurn("t3", "assistant", profileId = "p", createdAt = Instant.now()))
        artifactStore.save(ArtifactRecord("a1", "c3", null, "sql-query", mapOf("sql" to "SELECT 1"), createdAt = Instant.now()))
        store.attachArtifacts("c3", "t3", listOf("a1"))
        val record = store.load("c3")!!
        assertThat(record.turns[0].artifactIds).containsExactly("a1")
    }

    @Test
    fun `attachArtifacts is idempotent`() {
        seedChat("c4")
        store.appendTurn("c4", ConversationTurn("t4", "assistant", profileId = "p", createdAt = Instant.now()))
        artifactStore.save(ArtifactRecord("a2", "c4", null, "sql-query", mapOf(), createdAt = Instant.now()))
        store.attachArtifacts("c4", "t4", listOf("a2"))
        store.attachArtifacts("c4", "t4", listOf("a2"))
        val record = store.load("c4")!!
        assertThat(record.turns[0].artifactIds).hasSize(1)
    }

    @Test
    fun `appendTurn links artifactIds passed on the turn`() {
        seedChat("c6")
        artifactStore.save(ArtifactRecord("a5", "c6", null, "sql.generated", mapOf("sql" to "SELECT 1"), createdAt = Instant.now()))
        store.appendTurn(
            "c6",
            ConversationTurn("t6", "assistant", profileId = "p", createdAt = Instant.now(), artifactIds = listOf("a5")),
        )
        val record = store.load("c6")!!
        assertThat(record.turns[0].artifactIds).containsExactly("a5")
    }

    @Test
    fun `appendTurn links artifacts when artifact persisted before turn row exists`() {
        seedChat("c7")
        val turnId = "t7"
        artifactStore.save(
            ArtifactRecord(
                "a6",
                "c7",
                null,
                "sql.generated",
                mapOf("payload" to mapOf("artifactType" to "generated-sql", "sql" to "SELECT 1")),
                turnId = turnId,
                createdAt = Instant.now(),
            ),
        )
        store.appendTurn("c7", ConversationTurn(turnId, "assistant", profileId = "p", createdAt = Instant.now(), artifactIds = listOf("a6")))
        val record = store.load("c7")!!
        assertThat(record.turns[0].artifactIds).containsExactly("a6")
    }
}
