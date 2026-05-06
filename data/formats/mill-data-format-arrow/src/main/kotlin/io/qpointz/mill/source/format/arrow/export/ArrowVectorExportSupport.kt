package io.qpointz.mill.source.format.arrow.export

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.SchemaField
import io.qpointz.mill.source.format.arrow.ArrowRecordWriter
import io.qpointz.mill.source.format.arrow.ArrowWriterSettings
import io.qpointz.mill.sql.RecordReader
import io.qpointz.mill.sql.RecordReaders
import io.qpointz.mill.types.sql.DatabaseType
import io.qpointz.mill.vectors.VectorBlockIterator
import java.io.OutputStream

/**
 * Executes [block] then invokes [RecordReader.close]; [RecordReader] does not implement [AutoCloseable].
 */
private inline fun <R> RecordReader.useReader(block: (RecordReader) -> R): R {
    try {
        return block(this)
    } finally {
        close()
    }
}

internal fun RecordReader.toMillRecordSchema(): RecordSchema {
    val fields = mutableListOf<SchemaField>()
    for (i in 0 until columnCount) {
        fields.add(SchemaField(getColumnMetadata(i).name, i, DatabaseType.string(true, -1)))
    }
    return RecordSchema(fields)
}

internal fun RecordReader.rowToRecord(): io.qpointz.mill.source.Record {
    val m = LinkedHashMap<String, Any?>()
    for (i in 0 until columnCount) {
        val name = getColumnMetadata(i).name
        m[name] = if (isNull(i)) null else getObject(i)
    }
    return io.qpointz.mill.source.Record(m)
}

/**
 * Arrow IPC **streaming** export over [VectorBlockIterator] (one record batch per row, UTF-8 columns).
 */
internal object ArrowVectorExportSupport {

    /**
     * @param iterator vector batch source
     * @param out Arrow IPC stream bytes
     */
    fun writeIpcStream(iterator: VectorBlockIterator, out: OutputStream) {
        RecordReaders.recordReader(iterator).useReader { rr ->
            if (!rr.hasNext()) {
                return@useReader
            }
            val schema = rr.toMillRecordSchema()
            val settings = ArrowWriterSettings(schema)
            ArrowRecordWriter(settings, out).use { writer ->
                writer.open()
                while (rr.hasNext()) {
                    rr.next()
                    writer.write(rr.rowToRecord())
                }
            }
        }
    }
}
