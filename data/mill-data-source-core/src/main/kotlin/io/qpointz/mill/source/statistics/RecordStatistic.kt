package io.qpointz.mill.source.statistics

/**
 * Record-count slice for a blob or logical table.
 *
 * @property estimatedRowCount row count from format metadata when known, otherwise null
 */
data class RecordStatistic(
    val estimatedRowCount: Long?,
)
