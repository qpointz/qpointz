package io.qpointz.mill.persistence.ai.jpa.adapters

import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshOutcome
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateRepository
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateSnapshot
import io.qpointz.mill.ai.valuemap.state.ValueMappingRunState
import io.qpointz.mill.persistence.ai.jpa.entities.AiValueMappingStateEntity
import io.qpointz.mill.persistence.ai.jpa.repositories.AiValueMappingStateJpaRepository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * JPA implementation of [ValueMappingRefreshStateRepository] (WI-184).
 */
open class JpaValueMappingRefreshStateAdapter(
    private val jpa: AiValueMappingStateJpaRepository,
) : ValueMappingRefreshStateRepository {

    @Transactional
    override fun findByEntityRes(entityRes: String): ValueMappingRefreshStateSnapshot? =
        jpa.findById(entityRes).map { toSnapshot(it) }.orElse(null)

    @Transactional
    override fun markRunBeginning(entityRes: String) {
        val row = loadOrCreate(entityRes)
        row.currentState = ValueMappingRunState.REFRESHING.name
        row.refreshProgressPercent = 0
        row.updatedAt = Instant.now()
        jpa.save(row)
    }

    @Transactional
    override fun updateProgressPercent(entityRes: String, refreshProgressPercent: Int) {
        val row = loadOrCreate(entityRes)
        row.refreshProgressPercent = refreshProgressPercent
        row.updatedAt = Instant.now()
        jpa.save(row)
    }

    @Transactional
    override fun markIdleAfterSync(
        entityRes: String,
        lastRefreshStatus: ValueMappingRefreshOutcome,
        lastRefreshValuesCount: Long?,
        lastRefreshAt: Instant,
        statusDetail: String?,
        refreshProgressPercent: Int?,
        nextScheduledRefreshAt: Instant?,
    ) {
        val row = loadOrCreate(entityRes)
        row.currentState = ValueMappingRunState.IDLE.name
        row.lastRefreshStatus = lastRefreshStatus.name
        row.lastRefreshValuesCount = lastRefreshValuesCount
        row.lastRefreshAt = lastRefreshAt
        row.statusDetail = statusDetail
        row.refreshProgressPercent = refreshProgressPercent
        row.nextScheduledRefreshAt = nextScheduledRefreshAt
        row.updatedAt = Instant.now()
        jpa.save(row)
    }

    @Transactional
    override fun markStale(entityRes: String, statusDetail: String?) {
        val row = loadOrCreate(entityRes)
        row.currentState = ValueMappingRunState.IDLE.name
        row.lastRefreshStatus = ValueMappingRefreshOutcome.STALE.name
        row.statusDetail = statusDetail
        row.updatedAt = Instant.now()
        jpa.save(row)
    }

    private fun loadOrCreate(entityRes: String): AiValueMappingStateEntity =
        jpa.findById(entityRes).orElseGet {
            AiValueMappingStateEntity(
                entityRes = entityRes,
                lastRefreshStatus = ValueMappingRefreshOutcome.FAILED.name,
                currentState = ValueMappingRunState.IDLE.name,
            )
        }

    private fun toSnapshot(e: AiValueMappingStateEntity): ValueMappingRefreshStateSnapshot =
        ValueMappingRefreshStateSnapshot(
            entityRes = e.entityRes,
            lastRefreshAt = e.lastRefreshAt,
            nextScheduledRefreshAt = e.nextScheduledRefreshAt,
            lastRefreshValuesCount = e.lastRefreshValuesCount,
            lastRefreshStatus = ValueMappingRefreshOutcome.valueOf(e.lastRefreshStatus),
            currentState = ValueMappingRunState.valueOf(e.currentState),
            refreshProgressPercent = e.refreshProgressPercent,
            statusDetail = e.statusDetail,
            updatedAt = e.updatedAt,
        )
}
