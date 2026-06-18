package io.qpointz.mill.source.statistics

import io.qpointz.mill.source.RecordSchema

/**
 * Table-level key provider using schema conventions.
 *
 * Today: a single column named `id` is treated as a unique key.
 */
class SchemaKeyStatisticProvider(
    private val schema: RecordSchema,
) : KeyStatisticProvider {

    private val cached: KeyStatistic? by lazy { inferKeys() }

    override fun keyStatistic(): KeyStatistic? = cached

    private fun inferKeys(): KeyStatistic? {
        val idField = schema.field("id") ?: return null
        return KeyStatistic(uniqueKeys = listOf(listOf(idField.index)))
    }
}
