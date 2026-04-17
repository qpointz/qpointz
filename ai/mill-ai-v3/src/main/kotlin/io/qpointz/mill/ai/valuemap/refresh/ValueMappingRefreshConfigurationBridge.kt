package io.qpointz.mill.ai.valuemap.refresh

/**
 * Subset of `mill.ai.value-mapping.refresh.*` for the refresh orchestrator (implemented by autoconfigure).
 */
interface ValueMappingRefreshConfigurationBridge {

    /** Global gate for [ValueMappingRefreshRunKind.APP_STARTUP]. */
    val refreshStartupEnabled: Boolean

    /** When `true`, scheduled ticks are disabled. */
    val refreshScheduledDisabled: Boolean
}
