package io.qpointz.mill.source.statistics

/**
 * Primary-key slice for a logical table.
 *
 * @property uniqueKeys column index sets; each inner list is one unique key
 */
data class KeyStatistic(
    val uniqueKeys: List<List<Int>>,
)
