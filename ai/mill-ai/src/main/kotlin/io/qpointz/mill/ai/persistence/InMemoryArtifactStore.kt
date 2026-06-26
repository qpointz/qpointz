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

import java.util.concurrent.ConcurrentHashMap

class InMemoryArtifactStore : ArtifactStore {

    private val byId = ConcurrentHashMap<String, ArtifactRecord>()

    override fun save(artifact: ArtifactRecord) {
        byId[artifact.artifactId] = artifact
    }

    override fun delete(artifactId: String): Boolean = byId.remove(artifactId) != null

    override fun updateStatus(artifactId: String, status: ArtifactLifecycleStatus): ArtifactRecord? {
        val existing = byId[artifactId] ?: return null
        val updated = existing.copy(status = status)
        byId[artifactId] = updated
        return updated
    }

    override fun findById(artifactId: String): ArtifactRecord? = byId[artifactId]

    override fun findByConversation(conversationId: String): List<ArtifactRecord> =
        byId.values.filter { it.conversationId == conversationId }

    override fun findByRun(runId: String): List<ArtifactRecord> =
        byId.values.filter { it.runId == runId }
}





