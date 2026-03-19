package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaArtifactStore
import io.qpointz.mill.persistence.ai.jpa.repositories.ArtifactRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class JpaArtifactStoreIT {

    @Autowired lateinit var repo: ArtifactRepository
    private val store by lazy { JpaArtifactStore(repo) }

    @Test
    fun `should save and reload an artifact`() {
        val artifact = ArtifactRecord(
            artifactId = "art-1",
            conversationId = "c1",
            runId = "run-1",
            kind = "sql-query",
            payload = mapOf("sql" to "SELECT 1", "dialect" to "calcite"),
            pointerKeys = setOf("last-sql"),
            createdAt = Instant.now(),
        )
        store.save(artifact)
        val loaded = store.findById("art-1")
        assertThat(loaded).isNotNull
        assertThat(loaded!!.kind).isEqualTo("sql-query")
        assertThat(loaded.payload["sql"]).isEqualTo("SELECT 1")
        assertThat(loaded.pointerKeys).containsExactly("last-sql")
    }

    @Test
    fun `findByConversation returns artifacts in order`() {
        val t = Instant.now()
        store.save(ArtifactRecord("a1", "c2", null, "sql-query", mapOf(), createdAt = t.minusSeconds(2)))
        store.save(ArtifactRecord("a2", "c2", null, "chart-config", mapOf(), createdAt = t.minusSeconds(1)))
        val results = store.findByConversation("c2")
        assertThat(results.map { it.artifactId }).containsExactly("a1", "a2")
    }
}
