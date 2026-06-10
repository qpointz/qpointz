package io.qpointz.mill.ai.valuemap.state

/**
 * Derives [ValueMappingRefreshOutcome] from per-element success/failure counts (WI-184 / WI-182).
 */
object ValueMappingRefreshOutcomes {

    /**
     * Maps [ValueMappingSyncProgressCallback.onRunComplete] aggregates to a persisted outcome.
     *
     * **FAILED** — every element failed. **PARTIAL** — mixed. **COMPLETED** — none failed
     * (including the empty run `0` / `0`).
     */
    fun fromSuccessFailure(successCount: Int, failureCount: Int): ValueMappingRefreshOutcome =
        when {
            failureCount > 0 && successCount > 0 -> ValueMappingRefreshOutcome.PARTIAL
            failureCount > 0 -> ValueMappingRefreshOutcome.FAILED
            else -> ValueMappingRefreshOutcome.COMPLETED
        }
}
