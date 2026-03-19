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

/**
 * A single canonical chat turn in the durable transcript.
 *
 * Shape is intentionally minimal in the first pass (no parentTurnId, no status).
 * `text` may be null for artifact-oriented turns; normal user/assistant turns carry text.
 */
data class ConversationTurn(
    val turnId: String,
    /** "user" or "assistant" */
    val role: String,
    val text: String? = null,
    /** Ids of artifacts attached to this turn. */
    val artifactIds: List<String> = emptyList(),
    val createdAt: Instant,
)

/**
 * Full durable record for a single conversation.
 */
data class ConversationRecord(
    val conversationId: String,
    val profileId: String,
    /** Ordered list of turns; insertion order is authoritative. */
    val turns: List<ConversationTurn> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
)

/**
 * Port for canonical durable chat transcript.
 *
 * This is the authority for conversation reconstruction — not [ConversationSession.messages]
 * and not the raw event log.
 */
interface ConversationStore {
    fun appendTurn(conversationId: String, turn: ConversationTurn)
    fun attachArtifacts(conversationId: String, turnId: String, artifactIds: List<String>)
    fun load(conversationId: String): ConversationRecord?
    fun ensureExists(conversationId: String, profileId: String)
}





