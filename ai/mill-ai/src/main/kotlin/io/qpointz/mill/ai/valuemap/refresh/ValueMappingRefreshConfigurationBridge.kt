package io.qpointz.mill.ai.valuemap.refresh

import java.time.Duration

/**
 * Active `mill.ai.data.embedding.<profile>.refresh` settings for the refresh orchestrator (autoconfigure).
 */
interface ValueMappingRefreshConfigurationBridge {

    /** Global gate for [ValueMappingRefreshRunKind.APP_STARTUP]. */
    val refreshStartupEnabled: Boolean

    /** When `true`, scheduled ticks are disabled. */
    val refreshScheduledDisabled: Boolean

    /** Cadence for scheduled refresh ticks. */
    val refreshScheduleInterval: Duration
}
