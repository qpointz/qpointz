package io.qpointz.mill.source

import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InMemoryRecordSourceTest {

    private val schema = RecordSchema.of(
        "id" to DatabaseType.i32(false),
        "name" to DatabaseType.string(true, -1)
    )

    @Test
    fun shouldReturnSchema() {
        val source = InMemoryRecordSource.empty(schema)
        assertEquals(schema, source.schema)
    }

    @Test
    fun shouldIterateOverRecords() {
        val r1 = Record.of("id" to 1, "name" to "Alice")
        val r2 = Record.of("id" to 2, "name" to "Bob")
        val source = InMemoryRecordSource.of(schema, r1, r2)

        val result = source.toList()
        assertEquals(2, result.size)
        assertEquals(r1, result[0])
        assertEquals(r2, result[1])
    }

    @Test
    fun shouldReturnEmptyIterator_whenNoRecords() {
        val source = InMemoryRecordSource.empty(schema)
        assertFalse(source.iterator().hasNext())
        assertEquals(0, source.toList().size)
    }

    @Test
    fun shouldSupportMultipleIterations() {
        val r1 = Record.of("id" to 1, "name" to "Alice")
        val source = InMemoryRecordSource.of(schema, r1)

        assertEquals(1, source.toList().size)
        assertEquals(1, source.toList().size)
    }

    @Test
    fun shouldBeUsableAsFlowRecordSource() {
        val r1 = Record.of("id" to 1, "name" to "Alice")
        val source: FlowRecordSource = InMemoryRecordSource.of(schema, r1)

        assertEquals(schema, source.schema)
        assertEquals(1, source.toList().size)
    }

    @Test
    fun shouldPreserveRecordOrder() {
        val records = (1..100).map { Record.of("id" to it, "name" to "user-$it") }
        val source = InMemoryRecordSource(schema, records)

        val result = source.toList()
        assertEquals(100, result.size)
        result.forEachIndexed { idx, record ->
            assertEquals(idx + 1, record["id"])
        }
    }
}
