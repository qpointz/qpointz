package io.qpointz.mill.source.statistics

/**
 * Supplies [RecordStatistic] for one blob or an aggregated logical table.
 */
fun interface RecordStatisticProvider {

    /**
     * Returns the record statistic slice, or null when the value is unknown after evaluation.
     */
    fun recordStatistic(): RecordStatistic?
}
