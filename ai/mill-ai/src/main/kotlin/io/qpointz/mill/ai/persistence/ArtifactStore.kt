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
 * Durable record of a machine-readable artifact produced during a run.
 *
 * Full artifact history is kept; active/latest lookup is handled separately
 * by [ActiveArtifactPointerStore].
 */
data class ArtifactRecord(
    val artifactId: String,
    val conversationId: String,
    val runId: String?,
    /** Protocol-specific artifact kind (e.g. "sql-query", "chart-config"). */
    val kind: String,
    /** Structured, consumer-safe artifact payload. */
    val payload: Map<String, Any?>,
    /** Transcript turn this artifact belongs to, if linked. */
    val turnId: String? = null,
    /** Pointer keys to update in [ActiveArtifactPointerStore] after saving. */
    val pointerKeys: Set<String> = emptySet(),
    val createdAt: Instant,
)

/**
 * Port for durable artifact history.
 */
interface ArtifactStore {
    fun save(artifact: ArtifactRecord)
    fun findById(artifactId: String): ArtifactRecord?
    fun findByConversation(conversationId: String): List<ArtifactRecord>
    fun findByRun(runId: String): List<ArtifactRecord>
}





