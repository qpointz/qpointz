package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.format.avro.AvroRecordSource
import org.apache.avro.generic.GenericRecord
import org.apache.parquet.avro.AvroParquetReader
import org.apache.parquet.io.InputFile

/**
 * Row-oriented record source that reads a single Parquet file.
 *
 * Uses the Avro-based Parquet reader (`parquet-avro`) to read Parquet files
 * as [GenericRecord] instances, then converts them to Mill [Record]s.
 *
 * Accepts a Parquet [InputFile] so that reading works with any storage
 * backend (local filesystem, ADLS, S3, etc.) via [BlobInputFile].
 *
 * @property inputFile the Parquet file to read
 * @property schema    the Mill schema describing the expected fields
 */
class ParquetRecordSource(
    private val inputFile: InputFile,
    override val schema: RecordSchema
) : FlowRecordSource {

    override fun iterator(): Iterator<Record> {
        val reader = AvroParquetReader.builder<GenericRecord>(inputFile).build()

        return object : Iterator<Record> {
            private var next: GenericRecord? = reader.read()

            override fun hasNext(): Boolean {
                if (next == null) {
                    reader.close()
                }
                return next != null
            }

            override fun next(): Record {
                val current = next ?: run {
                    reader.close()
                    throw NoSuchElementException()
                }
                val record = toRecord(current)
                next = reader.read()
                return record
            }
        }
    }

    private fun toRecord(avroRecord: GenericRecord): Record {
        val values = schema.fields.associate { field ->
            field.name to AvroRecordSource.convertValue(avroRecord.get(field.name))
        }
        return Record(values)
    }
}
