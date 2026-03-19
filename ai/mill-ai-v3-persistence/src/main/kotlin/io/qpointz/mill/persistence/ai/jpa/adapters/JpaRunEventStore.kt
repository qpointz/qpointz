package io.qpointz.mill.persistence.ai.jpa.adapters

import io.qpointz.mill.ai.persistence.RunEventRecord
import io.qpointz.mill.ai.persistence.RunEventStore
import io.qpointz.mill.persistence.ai.jpa.entities.RunEventEntity
import io.qpointz.mill.persistence.ai.jpa.repositories.RunEventRepository
import org.springframework.transaction.annotation.Transactional

open class JpaRunEventStore(
    private val repo: RunEventRepository,
) : RunEventStore {

    @Transactional
    override fun save(record: RunEventRecord) {
        repo.save(record.toEntity())
    }

    override fun findByRun(runId: String): List<RunEventRecord> =
        repo.findByRunIdOrderByCreatedAtAsc(runId).map { it.toDomain() }

    private fun RunEventRecord.toEntity(): RunEventEntity =
        RunEventEntity(
            eventId = eventId,
            runId = runId,
            conversationId = conversationId,
            profileId = profileId,
            runtimeType = runtimeType,
            kind = kind,
            contentJson = content,
            createdAt = createdAt,
        )

    private fun RunEventEntity.toDomain(): RunEventRecord =
        RunEventRecord(
            eventId = eventId,
            runId = runId,
            conversationId = conversationId,
            profileId = profileId,
            runtimeType = runtimeType,
            kind = kind,
            content = contentJson,
            createdAt = createdAt,
        )
}
