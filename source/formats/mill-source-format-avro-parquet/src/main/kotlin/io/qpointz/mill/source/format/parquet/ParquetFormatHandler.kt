package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.*
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path as HadoopPath
import org.apache.parquet.hadoop.ParquetFileReader
import org.apache.parquet.hadoop.util.HadoopInputFile

/**
 * [FormatHandler] for Apache Parquet files.
 *
 * Infers schema from the Parquet file footer and creates row-oriented
 * [ParquetRecordSource] instances for reading data. Uses Hadoop filesystem
 * APIs for Parquet I/O compatibility.
 */
class ParquetFormatHandler : FormatHandler {

    /**
     * Infers the Mill [RecordSchema] from the Parquet file footer.
     *
     * Opens the blob via its URI, reads the footer metadata to obtain
     * the Parquet schema, and converts it via [ParquetSchemaConverter].
     */
    override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema {
        val hadoopPath = HadoopPath(blob.uri)
        val conf = Configuration()
        val inputFile = HadoopInputFile.fromPath(hadoopPath, conf)
        val reader = ParquetFileReader.open(inputFile)
        val parquetSchema = reader.fileMetaData.schema
        reader.close()
        return ParquetSchemaConverter.convert(parquetSchema)
    }

    /**
     * Creates a [ParquetRecordSource] for the given blob.
     *
     * The returned source reads Parquet data row-by-row via the Avro reader.
     */
    override fun createRecordSource(blob: BlobPath, blobSource: BlobSource, schema: RecordSchema): RecordSource {
        return ParquetRecordSource(blob.uri, schema)
    }
}
