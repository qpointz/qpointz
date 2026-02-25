package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import org.apache.arrow.memory.ArrowBuf
import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.FieldVector
import org.apache.arrow.vector.types.pojo.ArrowType
import org.apache.arrow.vector.types.pojo.Field
import org.apache.arrow.vector.util.Text
import java.nio.ByteBuffer
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Row-oriented Arrow IPC source. Reader path is timezone-aware for timestamp
 * fields with per-column timezone metadata and normalizes values to UTC instant.
 */
class ArrowRecordSource(
    private val blob: BlobPath,
    private val blobSource: BlobSource,
    override val schema: RecordSchema
) : FlowRecordSource {

    override fun iterator(): Iterator<Record> {
        val allocator = RootAllocator(Long.MAX_VALUE)
        val reader = ArrowReaders.open(blob, blobSource, allocator)
        return loadAllRecords(reader, allocator).iterator()
    }

    private fun loadAllRecords(reader: ArrowBatchReader, allocator: RootAllocator): List<Record> {
        return try {
            val records = mutableListOf<Record>()
            while (reader.loadNextBatch()) {
                val root = reader.root
                val fields = root.schema.fields
                val vectors = root.fieldVectors
                for (rowIndex in 0 until root.rowCount) {
                    val values = mutableMapOf<String, Any?>()
                    for (colIndex in fields.indices) {
                        val field = fields[colIndex]
                        val vector = vectors[colIndex]
                        values[field.name] = convertValue(vector, field, rowIndex)
                    }
                    records += Record(values)
                }
            }
            records
        } finally {
            runCatching { reader.close() }
            runCatching { allocator.close() }
        }
    }

    private fun convertValue(vector: FieldVector, field: Field, rowIndex: Int): Any? {
        if (vector.isNull(rowIndex)) return null
        val raw = vector.getObject(rowIndex)
        val base = normalizeGeneric(raw)

        val tsType = field.type as? ArrowType.Timestamp ?: return base
        if (tsType.timezone.isNullOrBlank()) return base
        return normalizeTimestampToUtc(base, tsType.timezone)
    }

    private fun normalizeGeneric(value: Any?): Any? {
        return when (value) {
            is Text -> value.toString()
            is ArrowBuf -> {
                val bytes = ByteArray(value.readableBytes().toInt())
                value.getBytes(value.readerIndex(), bytes)
                bytes
            }
            is ByteBuffer -> {
                val duplicate = value.duplicate()
                val bytes = ByteArray(duplicate.remaining())
                duplicate.get(bytes)
                bytes
            }
            else -> value
        }
    }

    private fun normalizeTimestampToUtc(value: Any?, timezone: String): Any? {
        if (value == null) return null
        val zone = ZoneId.of(timezone)

        return when (value) {
            is Instant -> value
            is ZonedDateTime -> value.toInstant()
            is OffsetDateTime -> value.toInstant()
            is LocalDateTime -> value.atZone(zone).toInstant()
            is Number -> Instant.ofEpochMilli(value.toLong())
            else -> value
        }
    }
}
