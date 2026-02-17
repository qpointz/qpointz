package io.qpointz.mill.source.format.avro

import io.qpointz.mill.source.*
import org.apache.avro.file.DataFileStream
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord

/**
 * [FormatHandler] for Apache Avro files.
 *
 * Infers schema from the Avro file header and creates row-oriented
 * [AvroRecordSource] instances for reading data.
 */
class AvroFormatHandler : FormatHandler {

    /**
     * Infers the Mill [RecordSchema] by reading the Avro file header.
     *
     * Opens the blob, reads the embedded Avro schema from the header,
     * converts it via [AvroSchemaConverter], and closes the stream.
     */
    override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema {
        val inputStream = blobSource.openInputStream(blob)
        return inputStream.use { stream ->
            val reader = GenericDatumReader<GenericRecord>()
            val dataFileStream = DataFileStream(stream, reader)
            val avroSchema = dataFileStream.schema
            dataFileStream.close()
            AvroSchemaConverter.convert(avroSchema)
        }
    }

    /**
     * Creates an [AvroRecordSource] for the given blob.
     *
     * The returned source reads Avro records row-by-row.
     */
    override fun createRecordSource(blob: BlobPath, blobSource: BlobSource, schema: RecordSchema): RecordSource {
        val inputStream = blobSource.openInputStream(blob)
        return AvroRecordSource(inputStream, schema)
    }
}
