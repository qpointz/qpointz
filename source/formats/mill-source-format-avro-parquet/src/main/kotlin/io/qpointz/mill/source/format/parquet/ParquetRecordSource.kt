package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.format.avro.AvroRecordSource
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path as HadoopPath
import org.apache.parquet.avro.AvroParquetReader
import org.apache.parquet.hadoop.util.HadoopInputFile
import java.net.URI

/**
 * Row-oriented record source that reads a single Parquet file.
 *
 * Uses the Avro-based Parquet reader (`parquet-avro`) to read Parquet files
 * as [GenericRecord] instances, then converts them to Mill [Record]s.
 *
 * @property fileUri URI pointing to the Parquet file
 * @property schema  the Mill schema describing the expected fields
 */
class ParquetRecordSource(
    private val fileUri: URI,
    override val schema: RecordSchema
) : FlowRecordSource {

    override fun iterator(): Iterator<Record> {
        val hadoopPath = HadoopPath(fileUri)
        val conf = Configuration()
        val inputFile = HadoopInputFile.fromPath(hadoopPath, conf)
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
