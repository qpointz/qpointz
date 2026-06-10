package io.qpointz.mill.persistence.ai.jpa.adapters

import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.persistence.ai.jpa.AiV3Urns
import io.qpointz.mill.persistence.ai.jpa.entities.ArtifactEntity
import io.qpointz.mill.persistence.ai.jpa.repositories.ArtifactRepository
import org.springframework.transaction.annotation.Transactional

open class JpaArtifactStore(
    private val repo: ArtifactRepository,
) : ArtifactStore {

    @Transactional
    override fun save(artifact: ArtifactRecord) {
        repo.save(artifact.toEntity())
    }

    override fun findById(artifactId: String): ArtifactRecord? =
        repo.findById(artifactId).orElse(null)?.toDomain()

    override fun findByConversation(conversationId: String): List<ArtifactRecord> =
        repo.findByConversationIdOrderByCreatedAtAsc(conversationId).map { it.toDomain() }

    override fun findByRun(runId: String): List<ArtifactRecord> =
        repo.findByRunIdOrderByCreatedAtAsc(runId).map { it.toDomain() }

    private fun ArtifactRecord.toEntity(): ArtifactEntity =
        ArtifactEntity(
            artifactId = artifactId,
            conversationId = conversationId,
            runId = runId,
            turnId = turnId,
            kind = kind,
            payloadJson = payload,
            pointerKeysJson = pointerKeys,
            urn = AiV3Urns.artifactUrn(artifactId),
            createdAt = createdAt,
        )

    private fun ArtifactEntity.toDomain(): ArtifactRecord =
        ArtifactRecord(
            artifactId = artifactId,
            conversationId = conversationId,
            runId = runId,
            kind = kind,
            payload = payloadJson,
            turnId = turnId,
            pointerKeys = pointerKeysJson,
            createdAt = createdAt,
        )
}
