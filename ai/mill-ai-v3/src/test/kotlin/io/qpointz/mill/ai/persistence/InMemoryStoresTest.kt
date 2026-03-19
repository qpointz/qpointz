package io.qpointz.mill.ai.persistence

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class InMemoryArtifactStoreTest {

    private val store = InMemoryArtifactStore()

    private fun artifact(id: String, convId: String = "c1", runId: String = "r1") = ArtifactRecord(
        artifactId = id,
        conversationId = convId,
        runId = runId,
        kind = "sql-query",
        payload = mapOf("sql" to "SELECT 1"),
        createdAt = Instant.now(),
    )

    @Test
    fun shouldSaveAndFindById() {
        store.save(artifact("a1"))
        assertNotNull(store.findById("a1"))
    }

    @Test
    fun shouldReturnNull_forMissingId() {
        assertNull(store.findById("missing"))
    }

    @Test
    fun shouldFindByConversation() {
        store.save(artifact("a1", convId = "conv-A"))
        store.save(artifact("a2", convId = "conv-A"))
        store.save(artifact("a3", convId = "conv-B"))
        assertEquals(2, store.findByConversation("conv-A").size)
    }

    @Test
    fun shouldFindByRun() {
        store.save(artifact("a1", runId = "run-X"))
        store.save(artifact("a2", runId = "run-X"))
        store.save(artifact("a3", runId = "run-Y"))
        assertEquals(2, store.findByRun("run-X").size)
    }
}

class InMemoryRunEventStoreTest {

    private val store = InMemoryRunEventStore()

    private fun record(runId: String) = RunEventRecord(
        eventId = java.util.UUID.randomUUID().toString(),
        runId = runId,
        conversationId = "c1",
        profileId = "p",
        kind = "run.started",
        runtimeType = "run.started",
        content = emptyMap(),
        createdAt = Instant.now(),
    )

    @Test
    fun shouldSaveAndFindByRun() {
        store.save(record("run-1"))
        store.save(record("run-1"))
        store.save(record("run-2"))
        assertEquals(2, store.findByRun("run-1").size)
    }

    @Test
    fun shouldReturnEmpty_forUnknownRun() {
        assertTrue(store.findByRun("none").isEmpty(), "expected empty list")
    }
}

class InMemoryConversationStoreTest {

    private val store = InMemoryConversationStore()

    private fun turn(role: String, text: String) = ConversationTurn(
        turnId = java.util.UUID.randomUUID().toString(),
        role = role,
        text = text,
        createdAt = Instant.now(),
    )

    @Test
    fun shouldCreateRecord_onEnsureExists() {
        store.ensureExists("conv-1", "profile-a")
        val record = store.load("conv-1")!!
        assertNotNull(record)
        assertEquals("profile-a", record.profileId)
        assertTrue(record.turns.isEmpty(), "expected empty turns")
    }

    @Test
    fun shouldNotOverwrite_whenEnsureExistsCalledTwice() {
        store.ensureExists("conv-1", "profile-a")
        store.appendTurn("conv-1", turn("user", "hello"))
        store.ensureExists("conv-1", "profile-b")
        assertEquals(1, store.load("conv-1")!!.turns.size)
    }

    @Test
    fun shouldAppendTurnsInOrder() {
        store.ensureExists("conv-1", "p")
        store.appendTurn("conv-1", turn("user", "hello"))
        store.appendTurn("conv-1", turn("assistant", "hi"))
        val record = store.load("conv-1")!!
        assertEquals(2, record.turns.size)
        assertEquals("user", record.turns[0].role)
        assertEquals("assistant", record.turns[1].role)
    }

    @Test
    fun shouldAttachArtifacts_toExistingTurn() {
        store.ensureExists("conv-1", "p")
        val turn = turn("assistant", "hi")
        store.appendTurn("conv-1", turn)
        store.attachArtifacts("conv-1", turn.turnId, listOf("a1", "a2"))
        val record = store.load("conv-1")!!
        assertEquals(listOf("a1", "a2"), record.turns[0].artifactIds)
    }

    @Test
    fun shouldReturnNull_forUnknownConversation() {
        assertNull(store.load("missing"))
    }
}

class InMemoryActiveArtifactPointerStoreTest {

    private val store = InMemoryActiveArtifactPointerStore()

    private fun pointer(convId: String, key: String, artId: String) = ActiveArtifactPointer(
        conversationId = convId,
        pointerKey = key,
        artifactId = artId,
        updatedAt = Instant.now(),
    )

    @Test
    fun shouldUpsertAndFind() {
        store.upsert(pointer("c1", "last-sql", "a1"))
        val found = store.find("c1", "last-sql")
        assertEquals("a1", found?.artifactId)
    }

    @Test
    fun shouldOverwrite_onSecondUpsert() {
        store.upsert(pointer("c1", "last-sql", "a1"))
        store.upsert(pointer("c1", "last-sql", "a2"))
        assertEquals("a2", store.find("c1", "last-sql")?.artifactId)
    }

    @Test
    fun shouldReturnNull_forMissing() {
        assertNull(store.find("c1", "last-sql"))
    }

    @Test
    fun shouldFindAll_forConversation() {
        store.upsert(pointer("c1", "last-sql", "a1"))
        store.upsert(pointer("c1", "last-chart", "a2"))
        store.upsert(pointer("c2", "last-sql", "a3"))
        assertEquals(2, store.findAll("c1").size)
    }
}





