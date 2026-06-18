package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.statistics.SourceTableStatisticProviders
import org.apache.calcite.schema.Statistic
import org.apache.calcite.schema.Statistics
import org.apache.calcite.util.ImmutableBitSet

/**
 * Maps Mill [SourceTableStatisticProviders] to Calcite [Statistic] values for [FlowTable].
 */
internal object FlowTableStatistics {

    /**
     * Builds Calcite statistics from wired slice providers on a [io.qpointz.mill.source.SourceTable].
     */
    fun toCalciteStatistic(providers: SourceTableStatisticProviders): Statistic {
        val rowCount = providers.recordStatistic()
            .map { it.recordStatistic()?.estimatedRowCount }
            .orElse(null)
            ?: return Statistics.UNKNOWN

        val keys = providers.keyStatistic()
            .map { it.keyStatistic()?.uniqueKeys.orEmpty() }
            .orElse(emptyList())
            .map { columnIndexes -> ImmutableBitSet.of(*columnIndexes.toIntArray()) }

        return Statistics.of(rowCount.toDouble(), keys)
    }
}
