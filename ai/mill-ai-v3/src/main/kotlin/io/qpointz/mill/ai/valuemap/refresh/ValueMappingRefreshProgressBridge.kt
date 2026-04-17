package io.qpointz.mill.ai.valuemap.refresh

import io.qpointz.mill.ai.valuemap.ValueMappingSyncProgressCallback
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshOutcome
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshOutcomes
import io.qpointz.mill.ai.valuemap.state.ValueMappingRefreshStateRepository
import java.time.Instant

/**
 * Debounces per-element progress to ~10% steps on [ValueMappingRefreshStateRepository] (WI-184 § Integration).
 */
class ValueMappingRefreshProgressBridge(
    private val stateRepository: ValueMappingRefreshStateRepository,
    private val nextScheduledRefreshAt: Instant?,
) : ValueMappingSyncProgressCallback {

    private var totalValues: Int = 0
    private var lastPersistedDecile: Int = -1

    override fun onBegin(attributeUrn: String, totalValues: Int) {
        this.totalValues = totalValues
        stateRepository.markRunBeginning(attributeUrn)
    }

    override fun onElementProcessed(
        attributeUrn: String,
        index: Int,
        content: String,
        success: Boolean,
        detail: String?,
    ) {
        if (totalValues <= 0) {
            return
        }
        val processed = index + 1
        val pct = (processed * 100 / totalValues).coerceIn(0, 100)
        val decile = (pct / 10) * 10
        if (decile > lastPersistedDecile) {
            lastPersistedDecile = decile
            stateRepository.updateProgressPercent(attributeUrn, decile)
        }
    }

    override fun onRunComplete(attributeUrn: String, successCount: Int, failureCount: Int) {
        val outcome = ValueMappingRefreshOutcomes.fromSuccessFailure(successCount, failureCount)
        stateRepository.markIdleAfterSync(
            entityRes = attributeUrn,
            lastRefreshStatus = outcome,
            lastRefreshValuesCount = successCount.toLong(),
            lastRefreshAt = Instant.now(),
            statusDetail = null,
            refreshProgressPercent = 100,
            nextScheduledRefreshAt = nextScheduledRefreshAt,
        )
    }

    fun markFailed(entityRes: String, detail: String) {
        stateRepository.markIdleAfterSync(
            entityRes = entityRes,
            lastRefreshStatus = ValueMappingRefreshOutcome.FAILED,
            lastRefreshValuesCount = null,
            lastRefreshAt = Instant.now(),
            statusDetail = detail.take(4000),
            refreshProgressPercent = null,
            nextScheduledRefreshAt = nextScheduledRefreshAt,
        )
    }
}
