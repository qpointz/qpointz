package io.qpointz.mill.ai.valuemap.state

import java.time.Instant

/**
 * Read model for one `ai_value_mapping_state` row (WI-184).
 */
data class ValueMappingRefreshStateSnapshot(
    val entityRes: String,
    val lastRefreshAt: Instant?,
    val nextScheduledRefreshAt: Instant?,
    val lastRefreshValuesCount: Long?,
    val lastRefreshStatus: ValueMappingRefreshOutcome,
    val currentState: ValueMappingRunState,
    val refreshProgressPercent: Int?,
    val statusDetail: String?,
    val updatedAt: Instant,
)
