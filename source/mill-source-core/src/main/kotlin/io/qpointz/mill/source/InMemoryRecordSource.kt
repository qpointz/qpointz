package io.qpointz.mill.source

/**
 * An in-memory [FlowRecordSource] backed by a list of [Record] instances.
 *
 * Useful for testing and as a building block for bridging between
 * row-oriented and columnar data paths.
 *
 * @property schema  the schema describing the records
 * @property records the backing data
 */
class InMemoryRecordSource(
    override val schema: RecordSchema,
    private val records: List<Record>
) : FlowRecordSource {

    override fun iterator(): Iterator<Record> = records.iterator()

    companion object {
        /**
         * Creates an [InMemoryRecordSource] from a schema and vararg records.
         */
        fun of(schema: RecordSchema, vararg records: Record): InMemoryRecordSource =
            InMemoryRecordSource(schema, records.toList())

        /**
         * Creates an empty [InMemoryRecordSource] with the given schema.
         */
        fun empty(schema: RecordSchema): InMemoryRecordSource =
            InMemoryRecordSource(schema, emptyList())
    }
}
