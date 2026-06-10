package io.qpointz.mill.ai.valuemap.state

/**
 * Last completed outcome for a value-mapping refresh run (`ai_value_mapping_state.last_refresh_status`, WI-184).
 */
enum class ValueMappingRefreshOutcome {
    COMPLETED,
    PARTIAL,
    FAILED,
    STALE,
}

/**
 * In-flight vs idle run marker (`ai_value_mapping_state.current_state`, WI-184).
 */
enum class ValueMappingRunState {
    REFRESHING,
    IDLE,
}
