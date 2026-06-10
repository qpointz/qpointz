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

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class InMemoryConversationStore : ConversationStore {

    private val records = ConcurrentHashMap<String, ConversationRecord>()

    override fun ensureExists(conversationId: String, profileId: String) {
        records.computeIfAbsent(conversationId) {
            val now = Instant.now()
            ConversationRecord(
                conversationId = conversationId,
                profileId = profileId,
                turns = emptyList(),
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    override fun appendTurn(conversationId: String, turn: ConversationTurn) {
        records.compute(conversationId) { _, existing ->
            val now = Instant.now()
            if (existing == null) {
                ConversationRecord(
                    conversationId = conversationId,
                    profileId = "",
                    turns = listOf(turn),
                    createdAt = now,
                    updatedAt = now,
                )
            } else {
                existing.copy(turns = existing.turns + turn, updatedAt = now)
            }
        }
    }

    override fun attachArtifacts(conversationId: String, turnId: String, artifactIds: List<String>) {
        if (artifactIds.isEmpty()) return
        records.computeIfPresent(conversationId) { _, existing ->
            val updatedTurns = existing.turns.map { turn ->
                if (turn.turnId != turnId) turn
                else turn.copy(artifactIds = (turn.artifactIds + artifactIds).distinct())
            }
            existing.copy(turns = updatedTurns, updatedAt = Instant.now())
        }
    }

    override fun load(conversationId: String): ConversationRecord? = records[conversationId]

    override fun delete(conversationId: String) {
        records.remove(conversationId)
    }
}





