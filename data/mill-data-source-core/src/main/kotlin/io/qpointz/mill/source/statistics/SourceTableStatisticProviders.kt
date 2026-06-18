package io.qpointz.mill.source.statistics

import java.util.Optional

/**
 * Wired statistic providers exposed by a [io.qpointz.mill.source.SourceTable].
 *
 * Support for a slice is expressed by [Optional.isPresent] on the corresponding accessor.
 */
class SourceTableStatisticProviders private constructor(
    private val record: RecordStatisticProvider?,
    private val key: KeyStatisticProvider?,
) {

    /** Present when record statistics are supported for this table. */
    fun recordStatistic(): Optional<RecordStatisticProvider> = Optional.ofNullable(record)

    /** Present when key statistics are supported for this table. */
    fun keyStatistic(): Optional<KeyStatisticProvider> = Optional.ofNullable(key)

    companion object {

        /** Empty holder — no statistic slices wired. */
        fun none(): SourceTableStatisticProviders = of()

        /**
         * Builds a holder from optional slice providers.
         *
         * @param record record-count provider, or null when not supported
         * @param key key provider, or null when not supported
         */
        fun of(
            record: RecordStatisticProvider? = null,
            key: KeyStatisticProvider? = null,
        ): SourceTableStatisticProviders = SourceTableStatisticProviders(record, key)
    }
}
