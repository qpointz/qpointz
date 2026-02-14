package io.qpointz.mill.source

import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InMemoryRecordWriterTest {

    private val schema = RecordSchema.of(
        "id" to DatabaseType.i32(false),
        "name" to DatabaseType.string(true, -1)
    )

    @Test
    fun shouldWriteRecords_whenOpenedProperly() {
        val writer = InMemoryRecordWriter(schema)
        writer.open()
        writer.write(Record.of("id" to 1, "name" to "Alice"))
        writer.write(Record.of("id" to 2, "name" to "Bob"))
        writer.close()

        assertEquals(2, writer.size)
        assertEquals(1, writer.records[0]["id"])
        assertEquals("Bob", writer.records[1]["name"])
    }

    @Test
    fun shouldThrow_whenWritingBeforeOpen() {
        val writer = InMemoryRecordWriter(schema)
        assertThrows<IllegalStateException> {
            writer.write(Record.of("id" to 1, "name" to "Alice"))
        }
    }

    @Test
    fun shouldThrow_whenWritingAfterClose() {
        val writer = InMemoryRecordWriter(schema)
        writer.open()
        writer.close()
        assertThrows<IllegalStateException> {
            writer.write(Record.of("id" to 1, "name" to "Alice"))
        }
    }

    @Test
    fun shouldThrow_whenOpeningTwice() {
        val writer = InMemoryRecordWriter(schema)
        writer.open()
        assertThrows<IllegalStateException> {
            writer.open()
        }
    }

    @Test
    fun shouldThrow_whenOpeningAfterClose() {
        val writer = InMemoryRecordWriter(schema)
        writer.open()
        writer.close()
        assertThrows<IllegalStateException> {
            writer.open()
        }
    }

    @Test
    fun shouldReturnEmptyList_whenNoRecordsWritten() {
        val writer = InMemoryRecordWriter(schema)
        writer.open()
        writer.close()
        assertEquals(0, writer.size)
        assertTrue(writer.records.isEmpty())
    }

    @Test
    fun shouldConvertToRecordSource() {
        val writer = InMemoryRecordWriter(schema)
        writer.open()
        writer.write(Record.of("id" to 1, "name" to "Alice"))
        writer.write(Record.of("id" to 2, "name" to "Bob"))
        writer.close()

        val source = writer.toRecordSource()
        assertEquals(schema, source.schema)
        assertEquals(2, source.toList().size)
        assertEquals(1, source.toList()[0]["id"])
    }

    @Test
    fun shouldReturnSnapshotOfRecords() {
        val writer = InMemoryRecordWriter(schema)
        writer.open()
        writer.write(Record.of("id" to 1, "name" to "Alice"))
        val snapshot = writer.records
        writer.write(Record.of("id" to 2, "name" to "Bob"))
        // snapshot should not change
        assertEquals(1, snapshot.size)
        assertEquals(2, writer.records.size)
    }

    @Test
    fun shouldWorkWithUseBlock() {
        val writer = InMemoryRecordWriter(schema)
        writer.use {
            it.open()
            it.write(Record.of("id" to 1, "name" to "Alice"))
        }
        assertEquals(1, writer.size)
    }

    @Test
    fun shouldExposeSchema() {
        val writer = InMemoryRecordWriter(schema)
        assertEquals(schema, writer.schema)
    }
}
