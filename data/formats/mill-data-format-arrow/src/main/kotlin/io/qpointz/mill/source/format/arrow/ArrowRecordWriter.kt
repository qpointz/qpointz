package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.source.FlowRecordWriter
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import org.apache.arrow.memory.BufferAllocator
import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.*
import org.apache.arrow.vector.ipc.ArrowStreamWriter
import org.apache.arrow.vector.types.pojo.Field
import org.apache.arrow.vector.types.pojo.Schema
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class ArrowWriterSettings(
    val schema: RecordSchema,
    val timezoneByColumn: Map<String, String> = emptyMap()
)

/**
 * Writes records to Arrow IPC stream format.
 */
class ArrowRecordWriter(
    private val settings: ArrowWriterSettings,
    private val outputStream: OutputStream
) : FlowRecordWriter {

    private var allocator: BufferAllocator? = null
    private var root: VectorSchemaRoot? = null
    private var writer: ArrowStreamWriter? = null
    private var fields: List<Field> = emptyList()

    override fun open() {
        val alloc = RootAllocator(Long.MAX_VALUE)
        val schema: Schema = ArrowSchemaConverter.toArrowSchema(settings.schema, settings.timezoneByColumn)
        val schemaRoot = VectorSchemaRoot.create(schema, alloc)
        val streamWriter = ArrowStreamWriter(schemaRoot, null, outputStream)
        streamWriter.start()

        allocator = alloc
        root = schemaRoot
        writer = streamWriter
        fields = schema.fields
    }

    override fun write(record: Record) {
        val schemaRoot = root ?: throw IllegalStateException("Writer not opened. Call open() first.")
        val streamWriter = writer ?: throw IllegalStateException("Writer not opened. Call open() first.")

        val vectors = schemaRoot.fieldVectors
        schemaRoot.rowCount = 1

        for (idx in vectors.indices) {
            val field = fields[idx]
            val vector = vectors[idx]
            setVectorValue(vector, field, record[field.name])
        }

        streamWriter.writeBatch()
    }

    override fun close() {
        runCatching { writer?.end() }
        runCatching { writer?.close() }
        runCatching { root?.close() }
        runCatching { allocator?.close() }
        writer = null
        root = null
        allocator = null
        fields = emptyList()
    }

    private fun setVectorValue(vector: FieldVector, field: Field, value: Any?) {
        if (value == null) {
            vector.setNull(0)
            return
        }

        when (vector) {
            is TinyIntVector -> vector.setSafe(0, (value as Number).toByte().toInt())
            is SmallIntVector -> vector.setSafe(0, (value as Number).toShort().toInt())
            is IntVector -> vector.setSafe(0, (value as Number).toInt())
            is BigIntVector -> vector.setSafe(0, (value as Number).toLong())
            is Float4Vector -> vector.setSafe(0, (value as Number).toFloat())
            is Float8Vector -> vector.setSafe(0, (value as Number).toDouble())
            is BitVector -> vector.setSafe(0, if (value as Boolean) 1 else 0)
            is VarCharVector -> vector.setSafe(0, value.toString().toByteArray(StandardCharsets.UTF_8))
            is VarBinaryVector -> {
                val bytes = when (value) {
                    is ByteArray -> value
                    else -> value.toString().toByteArray(StandardCharsets.UTF_8)
                }
                vector.setSafe(0, bytes)
            }
            is FixedSizeBinaryVector -> {
                val bytes = when (value) {
                    is ByteArray -> value
                    else -> value.toString().toByteArray(StandardCharsets.UTF_8)
                }
                vector.set(0, bytes.copyOf(vector.byteWidth))
            }
            is DateDayVector -> vector.setSafe(0, (value as Number).toInt())
            is TimeNanoVector -> vector.setSafe(0, (value as Number).toLong())
            is TimeStampMilliVector -> vector.setSafe(0, asEpochMillis(value))
            is TimeStampMilliTZVector -> vector.setSafe(0, asEpochMillisWithZone(value, field.name))
            else -> throw IllegalArgumentException("Unsupported Arrow vector type: ${vector::class.java.simpleName}")
        }
    }

    private fun asEpochMillis(value: Any): Long {
        return when (value) {
            is Number -> value.toLong()
            is Instant -> value.toEpochMilli()
            is LocalDateTime -> value.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()
            else -> Instant.parse(value.toString()).toEpochMilli()
        }
    }

    private fun asEpochMillisWithZone(value: Any, fieldName: String): Long {
        return when (value) {
            is Number -> value.toLong()
            is Instant -> value.toEpochMilli()
            is LocalDateTime -> {
                val zone = settings.timezoneByColumn[fieldName] ?: "UTC"
                value.atZone(ZoneId.of(zone)).toInstant().toEpochMilli()
            }
            else -> Instant.parse(value.toString()).toEpochMilli()
        }
    }
}
