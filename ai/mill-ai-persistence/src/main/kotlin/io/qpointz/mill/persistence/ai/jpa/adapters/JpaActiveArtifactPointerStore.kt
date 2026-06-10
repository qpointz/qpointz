package io.qpointz.mill.persistence.ai.jpa.adapters

import io.qpointz.mill.ai.persistence.ActiveArtifactPointer
import io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore
import io.qpointz.mill.persistence.ai.jpa.entities.ActiveArtifactPointerEntity
import io.qpointz.mill.persistence.ai.jpa.entities.ActiveArtifactPointerKey
import io.qpointz.mill.persistence.ai.jpa.repositories.ActiveArtifactPointerRepository
import org.springframework.transaction.annotation.Transactional

open class JpaActiveArtifactPointerStore(
    private val repo: ActiveArtifactPointerRepository,
) : ActiveArtifactPointerStore {

    @Transactional
    override fun upsert(pointer: ActiveArtifactPointer) {
        repo.save(
            ActiveArtifactPointerEntity(
                id = ActiveArtifactPointerKey(
                    conversationId = pointer.conversationId,
                    pointerKey = pointer.pointerKey,
                ),
                artifactId = pointer.artifactId,
                updatedAt = pointer.updatedAt,
            )
        )
    }

    override fun find(conversationId: String, pointerKey: String): ActiveArtifactPointer? =
        repo.findById(ActiveArtifactPointerKey(conversationId, pointerKey))
            .orElse(null)
            ?.toDomain()

    override fun findAll(conversationId: String): List<ActiveArtifactPointer> =
        repo.findByIdConversationId(conversationId).map { it.toDomain() }

    private fun ActiveArtifactPointerEntity.toDomain(): ActiveArtifactPointer =
        ActiveArtifactPointer(
            conversationId = id.conversationId,
            pointerKey = id.pointerKey,
            artifactId = artifactId,
            updatedAt = updatedAt,
        )
}
