package io.qpointz.mill.ai.autoconfigure.valuemap

import io.qpointz.mill.ai.data.valuemap.refresh.ValueMappingRefreshOrchestrator
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingRefreshConfigurationBridge
import jakarta.annotation.PostConstruct
import org.springframework.scheduling.TaskScheduler

/**
 * Fixed-delay scheduled pass for value-mapping refresh (WI-182). Registered only when scheduling is enabled.
 */
class ValueMappingRefreshScheduler(
    private val orchestrator: ValueMappingRefreshOrchestrator,
    private val config: ValueMappingRefreshConfigurationBridge,
    private val taskScheduler: TaskScheduler,
) {

    @PostConstruct
    fun register() {
        if (config.refreshScheduledDisabled) {
            return
        }
        taskScheduler.scheduleWithFixedDelay(
            { orchestrator.runScheduledTick() },
            config.refreshScheduleInterval,
        )
    }
}
