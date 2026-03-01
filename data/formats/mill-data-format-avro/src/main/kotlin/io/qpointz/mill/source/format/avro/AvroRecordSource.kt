package io.qpointz.mill.source.format.avro

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.CloseableRecordIterator
import org.apache.avro.file.DataFileStream
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import java.io.InputStream

/**
 * Row-oriented record source that reads a single Avro file.
 *
 * Opens the provided [inputStream] and reads Avro records one by one,
 * converting each [GenericRecord] to a Mill [Record] using the given [schema].
 *
 * The input stream is consumed lazily during iteration and closed when
 * the iterator is exhausted.
 *
 * @property inputStream the Avro file input stream
 * @property schema      the Mill schema describing the expected fields
 */
class AvroRecordSource(
    private val inputStreamSupplier: () -> InputStream,
    override val schema: RecordSchema
) : FlowRecordSource {

    constructor(
        inputStream: InputStream,
        schema: RecordSchema
    ) : this({ inputStream }, schema)

    override fun iterator(): Iterator<Record> {
        val reader = GenericDatumReader<GenericRecord>()
        val dataFileStream = DataFileStream(inputStreamSupplier(), reader)
        return AvroRecordIterator(dataFileStream, ::toRecord)
    }

    private fun toRecord(avroRecord: GenericRecord): Record {
        val values = schema.fields.associate { field ->
            val value = avroRecord.get(field.name)
            field.name to convertValue(value)
        }
        return Record(values)
    }

    companion object {

        /**
         * Converts an Avro value to a Mill-compatible value.
         *
         * Avro [org.apache.avro.util.Utf8] is converted to [String].
         * [java.nio.ByteBuffer] is converted to [ByteArray].
         * [GenericRecord] and other complex types are converted to string representation.
         */
        fun convertValue(value: Any?): Any? {
            if (value == null) return null
            return when (value) {
                is org.apache.avro.util.Utf8 -> value.toString()
                is java.nio.ByteBuffer -> {
                    val bytes = ByteArray(value.remaining())
                    value.get(bytes)
                    bytes
                }
                is org.apache.avro.generic.GenericFixed -> value.bytes().copyOf()
                is org.apache.avro.generic.GenericEnumSymbol<*> -> value.toString()
                is Boolean, is Int, is Long, is Float, is Double -> value
                else -> value.toString()
            }
        }
    }
}

private class AvroRecordIterator(
    private val dataFileStream: DataFileStream<GenericRecord>,
    private val mapper: (GenericRecord) -> Record
) : CloseableRecordIterator {
    private var closed = false

    override fun hasNext(): Boolean {
        val has = dataFileStream.hasNext()
        if (!has) {
            close()
        }
        return has
    }

    override fun next(): Record {
        if (!dataFileStream.hasNext()) {
            close()
            throw NoSuchElementException()
        }
        return mapper(dataFileStream.next())
    }

    override fun close() {
        if (closed) return
        closed = true
        dataFileStream.close()
    }
}
