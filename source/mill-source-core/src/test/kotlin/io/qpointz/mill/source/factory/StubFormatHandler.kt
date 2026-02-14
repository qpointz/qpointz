package io.qpointz.mill.source.factory

import io.qpointz.mill.source.*
import io.qpointz.mill.source.descriptor.FormatDescriptor
import io.qpointz.mill.source.descriptor.StubFormatDescriptor
import io.qpointz.mill.types.sql.DatabaseType

/**
 * Test-only [FormatHandler] that reads blob paths as single-column "filename" records.
 *
 * Used in materializer/resolver tests without requiring real file parsing.
 */
class StubFormatHandler(private val delimiter: String = ",") : FormatHandler {

    companion object {
        val STUB_SCHEMA = RecordSchema.of(
            "filename" to DatabaseType.string(false, -1)
        )
    }

    override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema = STUB_SCHEMA

    override fun createRecordSource(blob: BlobPath, blobSource: BlobSource, schema: RecordSchema): RecordSource {
        // Return a single-row source containing the blob's URI path as "filename"
        val record = Record.of("filename" to (blob.uri.path ?: blob.uri.toString()))
        return InMemoryRecordSource.of(schema, record)
    }
}

/**
 * Test-only [FormatHandlerFactory] for [StubFormatDescriptor].
 */
class StubFormatHandlerFactory : FormatHandlerFactory {

    override val descriptorType: Class<out FormatDescriptor>
        get() = StubFormatDescriptor::class.java

    override fun create(descriptor: FormatDescriptor): FormatHandler {
        require(descriptor is StubFormatDescriptor) {
            "Expected StubFormatDescriptor, got ${descriptor::class.java.name}"
        }
        return StubFormatHandler(delimiter = descriptor.delimiter)
    }
}
