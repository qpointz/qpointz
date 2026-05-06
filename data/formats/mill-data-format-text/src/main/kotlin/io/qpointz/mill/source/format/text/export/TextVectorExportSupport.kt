package io.qpointz.mill.source.format.text.export

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.SchemaField
import io.qpointz.mill.source.format.text.CsvRecordWriter
import io.qpointz.mill.source.format.text.CsvSettings
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

/**
 * Bridges [RecordReader] rows into SQL-oriented [io.qpointz.mill.source.Record] / [RecordSchema] for text export encoders.
 */
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
 * Delimited text streaming export (CSV / TSV) over [VectorBlockIterator] batches via [CsvRecordWriter].
 */
internal object TextVectorExportSupport {

    /**
     * @param iterator vector batch source
     * @param out response stream
     * @param delimiter field delimiter (comma for CSV, tab for TSV)
     */
    fun writeDelimited(iterator: VectorBlockIterator, out: OutputStream, delimiter: Char) {
        RecordReaders.recordReader(iterator).useReader { rr ->
            if (!rr.hasNext()) {
                return@useReader
            }
            val schema = rr.toMillRecordSchema()
            val settings = CsvSettings(delimiter = delimiter, hasHeader = true)
            CsvRecordWriter(schema, settings, out).use { writer ->
                writer.open()
                while (rr.next()) {
                    writer.write(rr.rowToRecord())
                }
            }
        }
    }
}
