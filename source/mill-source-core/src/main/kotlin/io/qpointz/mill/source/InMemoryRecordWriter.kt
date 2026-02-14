package io.qpointz.mill.source

/**
 * An in-memory [FlowRecordWriter] that collects written records into a list.
 *
 * After writing, the accumulated records can be retrieved via [records]
 * or converted into an [InMemoryRecordSource] via [toRecordSource].
 *
 * Primarily intended for testing.
 *
 * @property schema the schema that written records must conform to
 */
class InMemoryRecordWriter(
    val schema: RecordSchema
) : FlowRecordWriter {

    private val _records = mutableListOf<Record>()
    private var opened = false
    private var closed = false

    /** Returns the records written so far (unmodifiable snapshot). */
    val records: List<Record> get() = _records.toList()

    /** Returns the number of records written so far. */
    val size: Int get() = _records.size

    override fun open() {
        check(!closed) { "Writer is already closed" }
        check(!opened) { "Writer is already open" }
        opened = true
    }

    override fun write(record: Record) {
        check(opened) { "Writer must be opened before writing" }
        check(!closed) { "Writer is already closed" }
        _records.add(record)
    }

    override fun close() {
        closed = true
    }

    /**
     * Creates an [InMemoryRecordSource] from the records written to this writer.
     */
    fun toRecordSource(): InMemoryRecordSource =
        InMemoryRecordSource(schema, records)
}
