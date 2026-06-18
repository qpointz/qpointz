package io.qpointz.mill.source.statistics

/**
 * Supplies [KeyStatistic] for a logical table.
 */
fun interface KeyStatisticProvider {

    /**
     * Returns the key statistic slice, or null when keys cannot be determined.
     */
    fun keyStatistic(): KeyStatistic?
}
