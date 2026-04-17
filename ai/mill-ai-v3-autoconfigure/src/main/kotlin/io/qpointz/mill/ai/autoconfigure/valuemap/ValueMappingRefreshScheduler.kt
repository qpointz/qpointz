package io.qpointz.mill.ai.autoconfigure.valuemap

import io.qpointz.mill.ai.data.valuemap.refresh.ValueMappingRefreshOrchestrator
import org.springframework.scheduling.annotation.Scheduled

/**
 * Fixed-delay scheduled pass for value-mapping refresh (WI-182). Registered only when scheduling is enabled.
 */
class ValueMappingRefreshScheduler(
    private val orchestrator: ValueMappingRefreshOrchestrator,
) {

    @Scheduled(fixedDelayString = "\${mill.ai.value-mapping.refresh.schedule.interval:PT15M}")
    fun scheduledTick() {
        orchestrator.runScheduledTick()
    }
}
