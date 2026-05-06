package io.qpointz.mill.source.format.avro.export

import io.qpointz.mill.source.format.avro.AvroRecordWriter
import io.qpointz.mill.source.format.avro.AvroWriterSettings
import io.qpointz.mill.source.format.avro.JsonSchemaSource
import io.qpointz.mill.sql.RecordReader
import io.qpointz.mill.sql.RecordReaders
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

internal fun RecordReader.rowToRecord(): io.qpointz.mill.source.Record {
    val m = LinkedHashMap<String, Any?>()
    for (i in 0 until columnCount) {
        val name = getColumnMetadata(i).name
        m[name] = if (isNull(i)) null else getObject(i)
    }
    return io.qpointz.mill.source.Record(m)
}

/**
 * Avro OCF streaming export over [VectorBlockIterator] using a schema derived from the first batch.
 */
internal object AvroVectorExportSupport {

    /**
     * @param iterator vector batch source
     * @param out binary Avro container stream
     */
    fun writeContainerFile(iterator: VectorBlockIterator, out: OutputStream) {
        RecordReaders.recordReader(iterator).useReader { rr ->
            if (!rr.hasNext()) {
                return@useReader
            }
            val names = (0 until rr.columnCount).map { rr.getColumnMetadata(it).name }
            val json = avroSchemaJson(names)
            val settings = AvroWriterSettings(JsonSchemaSource(json))
            AvroRecordWriter(settings, out).use { writer ->
                writer.open()
                while (rr.next()) {
                    writer.write(rr.rowToRecord())
                }
            }
        }
    }

    private fun avroSchemaJson(fieldNames: List<String>): String {
        val fields = fieldNames.joinToString(",") { name ->
            val safe = name.replace("\"", "\\\"")
            """{"name":"$safe","type":["null","string"]}"""
        }
        return """{"type":"record","name":"ExportRow","fields":[$fields]}"""
    }
}
