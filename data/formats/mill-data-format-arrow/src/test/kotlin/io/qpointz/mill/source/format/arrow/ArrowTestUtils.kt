package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.types.logical.TimestampTZLogical
import io.qpointz.mill.types.sql.DatabaseType
import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.VectorSchemaRoot
import org.apache.arrow.vector.ipc.ArrowFileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object ArrowTestUtils {
    val testSchema: RecordSchema = RecordSchema.of(
        "id" to DatabaseType.i32(false),
        "name" to DatabaseType.string(true, -1),
        "active" to DatabaseType.bool(false),
        "event_ts" to DatabaseType.of(TimestampTZLogical.INSTANCE, true)
    )

    fun testRecords(): List<Record> = listOf(
        Record.of("id" to 1, "name" to "Alice", "active" to true, "event_ts" to "2026-01-01T10:00:00Z"),
        Record.of("id" to 2, "name" to null, "active" to false, "event_ts" to "2026-01-01T11:00:00Z"),
    )

    fun writeArrowStream(path: Path, schema: RecordSchema = testSchema, rows: List<Record> = testRecords()) {
        Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use { out ->
            ArrowRecordWriter(
                ArrowWriterSettings(schema, timezoneByColumn = mapOf("event_ts" to "UTC")),
                out
            ).use { writer ->
                writer.open()
                rows.forEach(writer::write)
            }
        }
    }

    fun writeArrowFile(path: Path, schema: RecordSchema = testSchema, rows: List<Record> = testRecords()) {
        val allocator = RootAllocator(Long.MAX_VALUE)
        allocator.use { alloc ->
            val arrowSchema = ArrowSchemaConverter.toArrowSchema(schema, mapOf("event_ts" to "UTC"))
            val root = VectorSchemaRoot.create(arrowSchema, alloc)
            root.use { schemaRoot ->
                Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE).use { channel ->
                    ArrowFileWriter(schemaRoot, null, channel).use { writer ->
                        writer.start()
                        rows.forEach { row ->
                            schemaRoot.rowCount = 1
                            val vectors = schemaRoot.fieldVectors
                            val fields = schemaRoot.schema.fields
                            for (i in vectors.indices) {
                                val value = row[fields[i].name]
                                val vector = vectors[i]
                                when (vector) {
                                    is org.apache.arrow.vector.IntVector -> if (value == null) vector.setNull(0) else vector.setSafe(0, (value as Number).toInt())
                                    is org.apache.arrow.vector.VarCharVector -> if (value == null) vector.setNull(0) else vector.setSafe(0, value.toString().toByteArray())
                                    is org.apache.arrow.vector.BitVector -> if (value == null) vector.setNull(0) else vector.setSafe(0, if (value as Boolean) 1 else 0)
                                    is org.apache.arrow.vector.TimeStampMilliTZVector -> if (value == null) vector.setNull(0) else vector.setSafe(0, java.time.Instant.parse(value.toString()).toEpochMilli())
                                }
                            }
                            writer.writeBatch()
                        }
                        writer.end()
                    }
                }
            }
        }
    }
}
