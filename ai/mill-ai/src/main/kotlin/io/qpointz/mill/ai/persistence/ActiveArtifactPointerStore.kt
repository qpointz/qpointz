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
 * Points to the most recent artifact for a given pointer key within a conversation.
 *
 * Supports refinement workflows where follow-up turns need to reference the last known
 * artifact of a given kind (e.g. "last-sql", "last-chart-config").
 */
data class ActiveArtifactPointer(
    val conversationId: String,
    /** Stable pointer key, e.g. "last-sql" or "last-chart-config". */
    val pointerKey: String,
    /** Id of the currently active artifact for this pointer. */
    val artifactId: String,
    val updatedAt: Instant,
)

/**
 * Port for tracking latest/active artifact pointers per conversation.
 */
interface ActiveArtifactPointerStore {
    fun upsert(pointer: ActiveArtifactPointer)
    fun find(conversationId: String, pointerKey: String): ActiveArtifactPointer?
    fun findAll(conversationId: String): List<ActiveArtifactPointer>
}





