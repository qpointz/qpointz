package io.qpointz.mill.persistence.ai.jpa.adapters

import io.qpointz.mill.ai.persistence.ActiveArtifactPointer
import io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore
import io.qpointz.mill.persistence.ai.jpa.entities.ActiveArtifactPointerEntity
import io.qpointz.mill.persistence.ai.jpa.entities.ActiveArtifactPointerKey
import io.qpointz.mill.persistence.ai.jpa.repositories.ActiveArtifactPointerRepository
import java.time.Instant
import org.springframework.transaction.annotation.Transactional

open class JpaActiveArtifactPointerStore(
    private val repo: ActiveArtifactPointerRepository,
) : ActiveArtifactPointerStore {

    @Transactional
    override fun upsert(pointer: ActiveArtifactPointer) {
        repo.deleteByIdChatIdAndIdPointerKey(pointer.conversationId, pointer.pointerKey)
        repo.save(pointer.toEntity())
    }

    @Transactional
    override fun appendAll(
        conversationId: String,
        pointerKey: String,
        artifactIds: List<String>,
        updatedAt: Instant,
    ) {
        artifactIds.forEachIndexed { index, artifactId ->
            repo.save(
                ActiveArtifactPointerEntity(
                    id = ActiveArtifactPointerKey(
                        chatId = conversationId,
                        pointerKey = pointerKey,
                        artifactId = artifactId,
                    ),
                    updatedAt = updatedAt.plusNanos(index.toLong()),
                ),
            )
        }
    }

    override fun find(conversationId: String, pointerKey: String): ActiveArtifactPointer? =
        findByPointerKey(conversationId, pointerKey).lastOrNull()

    override fun findByPointerKey(conversationId: String, pointerKey: String): List<ActiveArtifactPointer> =
        repo.findByIdChatIdAndIdPointerKeyOrderByUpdatedAtAsc(conversationId, pointerKey)
            .map { it.toDomain() }

    override fun findAll(conversationId: String): List<ActiveArtifactPointer> =
        repo.findByIdChatId(conversationId).map { it.toDomain() }

    private fun ActiveArtifactPointer.toEntity(): ActiveArtifactPointerEntity =
        ActiveArtifactPointerEntity(
            id = ActiveArtifactPointerKey(
                chatId = conversationId,
                pointerKey = pointerKey,
                artifactId = artifactId,
            ),
            updatedAt = updatedAt,
        )

    private fun ActiveArtifactPointerEntity.toDomain(): ActiveArtifactPointer =
        ActiveArtifactPointer(
            conversationId = id.chatId,
            pointerKey = id.pointerKey,
            artifactId = id.artifactId,
            updatedAt = updatedAt,
        )
}
