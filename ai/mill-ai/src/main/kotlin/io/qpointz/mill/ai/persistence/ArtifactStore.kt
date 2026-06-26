package io.qpointz.mill.ai.persistence

import io.qpointz.mill.ai.core.artifact.ArtifactRef
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
    /** Operator lifecycle for facet proposals and similar artefacts. */
    val status: ArtifactLifecycleStatus = ArtifactLifecycleStatus.ACTIVE,
    val createdAt: Instant,
) {
    /** Portable ref for this artefact instance. */
    fun ref(): ArtifactRef = ArtifactRef.of(artifactId)
}

/**
 * Port for durable artifact history.
 */
interface ArtifactStore {
    fun save(artifact: ArtifactRecord)
    fun findById(artifactId: String): ArtifactRecord?
    fun findByConversation(conversationId: String): List<ArtifactRecord>
    fun findByRun(runId: String): List<ArtifactRecord>

    /**
     * Removes a retracted artefact from durable storage.
     *
     * @param artifactId artefact primary key
     * @return `true` when a row existed and was removed
     */
    fun delete(artifactId: String): Boolean

    /**
     * Updates operator lifecycle status for an artefact.
     *
     * @param artifactId artefact primary key
     * @param status new lifecycle status
     * @return updated record, or `null` when missing
     */
    fun updateStatus(artifactId: String, status: ArtifactLifecycleStatus): ArtifactRecord?
}





