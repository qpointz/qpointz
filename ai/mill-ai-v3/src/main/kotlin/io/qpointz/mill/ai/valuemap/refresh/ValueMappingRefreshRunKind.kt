package io.qpointz.mill.ai.valuemap.refresh

/**
 * Origin of a value-mapping refresh pass (WI-182).
 */
enum class ValueMappingRefreshRunKind {
    APP_STARTUP,
    SCHEDULED_TICK,
    ON_DEMAND,
}
