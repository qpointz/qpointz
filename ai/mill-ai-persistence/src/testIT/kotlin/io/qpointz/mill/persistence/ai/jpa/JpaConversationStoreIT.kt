package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.persistence.ConversationTurn
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaConversationStore
import io.qpointz.mill.persistence.ai.jpa.repositories.AiRelationRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ArtifactRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ConversationRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.ConversationTurnRepository
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaArtifactStore
import io.qpointz.mill.ai.persistence.ArtifactRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
class JpaConversationStoreIT {

    @Autowired lateinit var conversationRepo: ConversationRepository
    @Autowired lateinit var turnRepo: ConversationTurnRepository
    @Autowired lateinit var relationRepo: AiRelationRepository
    @Autowired lateinit var artifactRepo: ArtifactRepository

    private val store by lazy { JpaConversationStore(conversationRepo, turnRepo, relationRepo, artifactRepo) }
    private val artifactStore by lazy { JpaArtifactStore(artifactRepo) }

    @Test
    fun `ensureExists is idempotent`() {
        store.ensureExists("c1", "profile-a")
        store.ensureExists("c1", "profile-a")
        assertThat(conversationRepo.findById("c1")).isPresent
    }

    @Test
    fun `appendTurn assigns position in order`() {
        store.ensureExists("c2", "p")
        store.appendTurn("c2", ConversationTurn("t1", "user", "hello", createdAt = Instant.now()))
        store.appendTurn("c2", ConversationTurn("t2", "assistant", "hi", createdAt = Instant.now()))
        val record = store.load("c2")!!
        assertThat(record.turns).hasSize(2)
        assertThat(record.turns[0].turnId).isEqualTo("t1")
        assertThat(record.turns[1].turnId).isEqualTo("t2")
    }

    @Test
    fun `attachArtifacts links artifacts to a turn`() {
        store.ensureExists("c3", "p")
        store.appendTurn("c3", ConversationTurn("t3", "assistant", createdAt = Instant.now()))
        artifactStore.save(ArtifactRecord("a1", "c3", null, "sql-query", mapOf("sql" to "SELECT 1"), createdAt = Instant.now()))
        store.attachArtifacts("c3", "t3", listOf("a1"))
        val record = store.load("c3")!!
        assertThat(record.turns[0].artifactIds).containsExactly("a1")
    }

    @Test
    fun `attachArtifacts is idempotent`() {
        store.ensureExists("c4", "p")
        store.appendTurn("c4", ConversationTurn("t4", "assistant", createdAt = Instant.now()))
        artifactStore.save(ArtifactRecord("a2", "c4", null, "sql-query", mapOf(), createdAt = Instant.now()))
        store.attachArtifacts("c4", "t4", listOf("a2"))
        store.attachArtifacts("c4", "t4", listOf("a2"))
        val record = store.load("c4")!!
        assertThat(record.turns[0].artifactIds).hasSize(1)
    }

    @Test
    fun `appendTurn links artifactIds passed on the turn`() {
        store.ensureExists("c6", "p")
        artifactStore.save(ArtifactRecord("a5", "c6", null, "sql.generated", mapOf("sql" to "SELECT 1"), createdAt = Instant.now()))
        store.appendTurn(
            "c6",
            ConversationTurn("t6", "assistant", createdAt = Instant.now(), artifactIds = listOf("a5")),
        )
        val record = store.load("c6")!!
        assertThat(record.turns[0].artifactIds).containsExactly("a5")
    }

    @Test
    fun `appendTurn links artifacts when artifact persisted before turn row exists`() {
        store.ensureExists("c7", "p")
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
        store.appendTurn("c7", ConversationTurn(turnId, "assistant", createdAt = Instant.now(), artifactIds = listOf("a6")))
        val record = store.load("c7")!!
        assertThat(record.turns[0].artifactIds).containsExactly("a6")
    }
}
