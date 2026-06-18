package io.qpointz.mill.source.statistics

/**
 * Aggregates per-blob [RecordStatisticProvider] instances into one logical-table provider.
 *
 * Returns null when any child slice is unknown. Memoizes the combined result.
 */
class RecordStatisticAggregator(
    private val providers: Iterable<RecordStatisticProvider>,
) : RecordStatisticProvider {

    private val cached: RecordStatistic? by lazy { aggregate() }

    override fun recordStatistic(): RecordStatistic? = cached

    private fun aggregate(): RecordStatistic? {
        val providerList = providers.toList()
        if (providerList.isEmpty()) {
            return null
        }
        val slices = providerList.map { it.recordStatistic() }
        if (slices.any { it?.estimatedRowCount == null }) {
            return null
        }
        return RecordStatistic(estimatedRowCount = slices.sumOf { it!!.estimatedRowCount!! })
    }
}
