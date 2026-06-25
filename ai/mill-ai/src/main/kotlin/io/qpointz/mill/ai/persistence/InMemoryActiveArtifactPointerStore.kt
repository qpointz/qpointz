package io.qpointz.mill.ai.persistence

import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [ActiveArtifactPointerStore] for tests and local runs.
 *
 * Keys are `conversationId::pointerKey::artifactId` so [appendAll] can retain **N** rows per
 * pointer key (list cardinality). [upsert] clears prior rows for the same conversation + pointer key.
 */
class InMemoryActiveArtifactPointerStore : ActiveArtifactPointerStore {

    private val store = ConcurrentHashMap<String, ActiveArtifactPointer>()

    private fun key(conversationId: String, pointerKey: String, artifactId: String) =
        "$conversationId::$pointerKey::$artifactId"

    override fun upsert(pointer: ActiveArtifactPointer) {
        store.keys.filter { it.startsWith("${pointer.conversationId}::${pointer.pointerKey}::") }
            .forEach { store.remove(it) }
        store[key(pointer.conversationId, pointer.pointerKey, pointer.artifactId)] = pointer
    }

    override fun appendAll(
        conversationId: String,
        pointerKey: String,
        artifactIds: List<String>,
        updatedAt: java.time.Instant,
    ) {
        artifactIds.forEachIndexed { index, artifactId ->
            val pointer = ActiveArtifactPointer(
                conversationId = conversationId,
                pointerKey = pointerKey,
                artifactId = artifactId,
                updatedAt = updatedAt.plusNanos(index.toLong()),
            )
            store[key(conversationId, pointerKey, artifactId)] = pointer
        }
    }

    override fun find(conversationId: String, pointerKey: String): ActiveArtifactPointer? =
        findByPointerKey(conversationId, pointerKey).maxByOrNull { it.updatedAt }

    override fun findByPointerKey(conversationId: String, pointerKey: String): List<ActiveArtifactPointer> =
        store.values
            .filter { it.conversationId == conversationId && it.pointerKey == pointerKey }
            .sortedBy { it.updatedAt }

    override fun findAll(conversationId: String): List<ActiveArtifactPointer> =
        store.values.filter { it.conversationId == conversationId }
}
