package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.FormatHandler
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.RecordSource
import org.apache.arrow.memory.RootAllocator

class ArrowFormatHandler : FormatHandler {
    override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema {
        val allocator = RootAllocator(Long.MAX_VALUE)
        return allocator.use { alloc ->
            ArrowReaders.open(blob, blobSource, alloc).use { reader ->
                ArrowSchemaConverter.toRecordSchema(reader.root.schema)
            }
        }
    }

    override fun createRecordSource(blob: BlobPath, blobSource: BlobSource, schema: RecordSchema): RecordSource {
        return ArrowRecordSource(blob, blobSource, schema)
    }
}
