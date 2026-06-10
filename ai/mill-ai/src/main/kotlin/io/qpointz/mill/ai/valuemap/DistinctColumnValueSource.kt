package io.qpointz.mill.ai.valuemap

/**
 * Column-backed DISTINCT values (WI-181): [context] prefixes each cell to form the embedding line;
 * [metadataValue] maps each cell to substitution [AttributeValueEntry.metadata] `value` (defaults to the cell).
 */
class DistinctColumnValueSource(
    private val context: String,
    private val distinctCellValues: List<String>,
    private val metadataValue: (String) -> String = { it },
) : ValueSource {

    override fun provideEntries(): List<AttributeValueEntry> =
        distinctCellValues.map { cell ->
            val line = context + cell
            AttributeValueEntry(
                content = line,
                metadata = mapOf("value" to metadataValue(cell)),
            )
        }
}
