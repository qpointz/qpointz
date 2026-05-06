package io.qpointz.mill.source.format.json.export

import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.SchemaField
import io.qpointz.mill.sql.RecordReader
import io.qpointz.mill.sql.RecordReaders
import io.qpointz.mill.types.sql.DatabaseType
import io.qpointz.mill.vectors.VectorBlockIterator
import tools.jackson.core.JsonGenerator
import tools.jackson.core.json.JsonFactory
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

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

private fun RecordReader.toMillRecordSchema(): RecordSchema {
    val fields = mutableListOf<SchemaField>()
    for (i in 0 until columnCount) {
        fields.add(SchemaField(getColumnMetadata(i).name, i, DatabaseType.string(true, -1)))
    }
    return RecordSchema(fields)
}

private fun RecordReader.rowToRecord(): Record {
    val m = LinkedHashMap<String, Any?>()
    for (i in 0 until columnCount) {
        val name = getColumnMetadata(i).name
        m[name] = if (isNull(i)) null else getObject(i)
    }
    return Record(m)
}

/**
 * Writes a UTF-8 JSON array of row objects by streaming through [JsonGenerator] (bounded memory).
 */
object JsonArrayStreamingEncoder {

    fun writeJsonArray(iterator: VectorBlockIterator, out: OutputStream) {
        val factory = JsonFactory()
        factory.createGenerator(OutputStreamWriter(out, StandardCharsets.UTF_8)).use { gen ->
            gen.writeStartArray()
            RecordReaders.recordReader(iterator).useReader { rr ->
                if (!rr.hasNext()) {
                    return@useReader
                }
                val schema = rr.toMillRecordSchema()
                while (rr.next()) {
                    writeRowObject(gen, schema, rr.rowToRecord())
                }
            }
            gen.writeEndArray()
        }
    }

    private fun writeRowObject(gen: JsonGenerator, schema: RecordSchema, row: Record) {
        gen.writeStartObject()
        for (f in schema.fields) {
            gen.writeName(f.name)
            val v = row[f.name]
            when (v) {
                null -> gen.writeNull()
                is String -> gen.writeString(v)
                is Int -> gen.writeNumber(v)
                is Long -> gen.writeNumber(v)
                is Short -> gen.writeNumber(v)
                is Byte -> gen.writeNumber(v.toShort())
                is Double -> gen.writeNumber(v)
                is Float -> gen.writeNumber(v)
                is java.math.BigInteger -> gen.writeNumber(v)
                is java.math.BigDecimal -> gen.writeNumber(v)
                is Number -> gen.writeNumber(v.toDouble())
                is Boolean -> gen.writeBoolean(v)
                else -> gen.writeString(v.toString())
            }
        }
        gen.writeEndObject()
    }
}
