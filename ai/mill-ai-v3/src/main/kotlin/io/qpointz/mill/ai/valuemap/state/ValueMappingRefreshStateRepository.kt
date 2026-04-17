package io.qpointz.mill.ai.valuemap.state

import java.time.Instant

/**
 * Persistence port for `ai_value_mapping_state` (WI-184). The WI-182 orchestrator owns when to call each method.
 */
interface ValueMappingRefreshStateRepository {

    /**
     * @return snapshot or `null` if no row exists yet
     */
    fun findByEntityRes(entityRes: String): ValueMappingRefreshStateSnapshot?

    /**
     * Sets [ValueMappingRunState.REFRESHING], resets progress for a new run.
     */
    fun markRunBeginning(entityRes: String)

    /**
     * Persists [refreshProgressPercent] while [ValueMappingRunState.REFRESHING] (WI-184 § Decisions — 10% steps).
     */
    fun updateProgressPercent(entityRes: String, refreshProgressPercent: Int)

    /**
     * Finalizes a sync run: [ValueMappingRunState.IDLE], [lastRefreshStatus], [lastRefreshValuesCount] semantics per WI-184.
     *
     * @param lastRefreshValuesCount count of **successful** elements in the last completed run; `null` when the run
     * did not complete in a way that yields a count (e.g. orchestrator policy); `0` when completed with zero successes.
     */
    fun markIdleAfterSync(
        entityRes: String,
        lastRefreshStatus: ValueMappingRefreshOutcome,
        lastRefreshValuesCount: Long?,
        lastRefreshAt: Instant,
        statusDetail: String?,
        refreshProgressPercent: Int?,
        nextScheduledRefreshAt: Instant?,
    )

    /**
     * Metadata still references the attribute but the physical catalog has no column (WI-184 **STALE**).
     */
    fun markStale(entityRes: String, statusDetail: String?)
}
