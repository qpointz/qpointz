package io.qpointz.mill.ai.valuemap

/**
 * Static facet rows: each pair is embedding [first] (full line) and substitution [second] in metadata [value].
 */
class StaticListValueSource(
    private val rows: List<Pair<String, String>>,
) : ValueSource {

    constructor(vararg rows: Pair<String, String>) : this(rows.toList())

    override fun provideEntries(): List<AttributeValueEntry> =
        rows.map { (content, value) ->
            AttributeValueEntry(
                content = content,
                metadata = mapOf("value" to value),
            )
        }
}
