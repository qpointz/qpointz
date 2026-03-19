package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.persistence.ActiveArtifactPointer
import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaActiveArtifactPointerStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaArtifactStore
import io.qpointz.mill.persistence.ai.jpa.repositories.ActiveArtifactPointerRepository
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
class JpaActiveArtifactPointerStoreIT {

    @Autowired lateinit var pointerRepo: ActiveArtifactPointerRepository
    @Autowired lateinit var artifactRepo: ArtifactRepository
    private val artifactStore by lazy { JpaArtifactStore(artifactRepo) }
    private val store by lazy { JpaActiveArtifactPointerStore(pointerRepo) }

    @Test
    fun `upsert and find pointer`() {
        artifactStore.save(ArtifactRecord("a1", "c1", null, "sql-query", mapOf(), createdAt = Instant.now()))
        val pointer = ActiveArtifactPointer("c1", "last-sql", "a1", Instant.now())
        store.upsert(pointer)
        val found = store.find("c1", "last-sql")
        assertThat(found).isNotNull
        assertThat(found!!.artifactId).isEqualTo("a1")
    }

    @Test
    fun `upsert replaces existing pointer`() {
        artifactStore.save(ArtifactRecord("a1", "c2", null, "sql-query", mapOf(), createdAt = Instant.now()))
        artifactStore.save(ArtifactRecord("a2", "c2", null, "sql-query", mapOf(), createdAt = Instant.now()))
        store.upsert(ActiveArtifactPointer("c2", "last-sql", "a1", Instant.now()))
        store.upsert(ActiveArtifactPointer("c2", "last-sql", "a2", Instant.now()))
        val found = store.find("c2", "last-sql")
        assertThat(found!!.artifactId).isEqualTo("a2")
    }

    @Test
    fun `findAll returns all pointers for conversation`() {
        artifactStore.save(ArtifactRecord("a1", "c3", null, "sql-query", mapOf(), createdAt = Instant.now()))
        artifactStore.save(ArtifactRecord("a2", "c3", null, "chart-config", mapOf(), createdAt = Instant.now()))
        store.upsert(ActiveArtifactPointer("c3", "last-sql", "a1", Instant.now()))
        store.upsert(ActiveArtifactPointer("c3", "last-chart", "a2", Instant.now()))
        val all = store.findAll("c3")
        assertThat(all).hasSize(2)
    }
}
