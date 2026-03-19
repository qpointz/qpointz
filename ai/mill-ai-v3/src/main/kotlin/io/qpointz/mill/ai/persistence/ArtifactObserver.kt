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

/**
 * Downstream observer notified after an artifact has been durably persisted.
 *
 * This hook is intentionally outside the critical chat loop. Implementations must be
 * best-effort and must not affect artifact persistence correctness.
 */
fun interface ArtifactObserver {
    fun onArtifactCreated(artifact: ArtifactRecord)
}

/**
 * Lightweight normalized description of an indexing request for a persisted artifact.
 */
data class ArtifactIndexingRequest(
    val artifactId: String,
    val conversationId: String,
    val runId: String?,
    val turnId: String?,
    val kind: String,
    val artifactType: String,
) {
    companion object {
        fun from(artifact: ArtifactRecord): ArtifactIndexingRequest {
            val payloadArtifactType = (artifact.payload["payload"] as? Map<*, *>)?.get("artifactType") as? String
                ?: artifact.payload["artifactType"] as? String
            return ArtifactIndexingRequest(
                artifactId = artifact.artifactId,
                conversationId = artifact.conversationId,
                runId = artifact.runId,
                turnId = artifact.turnId,
                kind = artifact.kind,
                artifactType = payloadArtifactType ?: artifact.kind,
            )
        }
    }
}

/**
 * First-pass /dev/null observer for WI-075.
 *
 * It distinguishes artifact types and prints indexing requests, but intentionally does not
 * derive or persist any relations yet.
 */
class NoOpArtifactObserver(
    private val sink: (String) -> Unit = ::println,
) : ArtifactObserver {

    override fun onArtifactCreated(artifact: ArtifactRecord) {
        val request = ArtifactIndexingRequest.from(artifact)
        sink(
            buildString {
                append("[artifact-indexer] noop request")
                append(" artifactId="); append(request.artifactId)
                append(" kind="); append(request.kind)
                append(" artifactType="); append(request.artifactType)
                append(" conversationId="); append(request.conversationId)
                request.runId?.let {
                    append(" runId="); append(it)
                }
                request.turnId?.let {
                    append(" turnId="); append(it)
                }
            }
        )
    }
}





