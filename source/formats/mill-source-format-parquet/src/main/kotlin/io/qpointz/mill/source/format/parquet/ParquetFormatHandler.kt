package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.*
import org.apache.parquet.hadoop.ParquetFileReader

/**
 * [FormatHandler] for Apache Parquet files.
 *
 * Infers schema from the Parquet file footer and creates row-oriented
 * [ParquetRecordSource] instances for reading data. Uses [BlobInputFile]
 * to read from any storage backend without Hadoop dependencies.
 */
class ParquetFormatHandler : FormatHandler {

    /**
     * Infers the Mill [RecordSchema] from the Parquet file footer.
     *
     * Opens the blob via [BlobInputFile], reads the footer metadata to obtain
     * the Parquet schema, and converts it via [ParquetSchemaConverter].
     */
    override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema {
        val inputFile = BlobInputFile(blob, blobSource)
        val reader = ParquetFileReader.open(inputFile)
        val parquetSchema = reader.fileMetaData.schema
        reader.close()
        return ParquetSchemaConverter.convert(parquetSchema)
    }

    /**
     * Creates a [ParquetRecordSource] for the given blob.
     *
     * The returned source reads Parquet data row-by-row via the Avro reader,
     * using [BlobInputFile] for storage-agnostic access.
     */
    override fun createRecordSource(blob: BlobPath, blobSource: BlobSource, schema: RecordSchema): RecordSource {
        val inputFile = BlobInputFile(blob, blobSource)
        return ParquetRecordSource(inputFile, schema)
    }
}
