package io.qpointz.mill.source

import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MultiFileSourceTableTest {

    private val schema = RecordSchema.of(
        "id" to DatabaseType.i32(false),
        "name" to DatabaseType.string(true, -1)
    )

    private fun makeSource(vararg records: Record): InMemoryRecordSource =
        InMemoryRecordSource(schema, records.toList())

    @Test
    fun shouldReturnSchema() {
        val table = MultiFileSourceTable.empty(schema)
        assertEquals(schema, table.schema)
    }

    @Test
    fun shouldReturnEmptyRecords_whenNoSources() {
        val table = MultiFileSourceTable.empty(schema)
        assertEquals(0, table.records().count())
    }

    @Test
    fun shouldConcatenateRecords_fromMultipleSources() {
        val src1 = makeSource(Record.of("id" to 1, "name" to "Alice"))
        val src2 = makeSource(Record.of("id" to 2, "name" to "Bob"))
        val src3 = makeSource(Record.of("id" to 3, "name" to "Charlie"))

        val table = MultiFileSourceTable(schema, listOf(src1, src2, src3))
        val records = table.records().toList()

        assertEquals(3, records.size)
        assertEquals(1, records[0]["id"])
        assertEquals(2, records[1]["id"])
        assertEquals(3, records[2]["id"])
    }

    @Test
    fun shouldConcatenateMultipleRecords_perSource() {
        val src1 = makeSource(
            Record.of("id" to 1, "name" to "Alice"),
            Record.of("id" to 2, "name" to "Bob")
        )
        val src2 = makeSource(
            Record.of("id" to 3, "name" to "Charlie")
        )

        val table = MultiFileSourceTable(schema, listOf(src1, src2))
        assertEquals(3, table.records().toList().size)
    }

    @Test
    fun shouldProduceVectorBlocks_fromRecordSources() {
        val src = makeSource(
            Record.of("id" to 1, "name" to "Alice"),
            Record.of("id" to 2, "name" to "Bob")
        )
        val table = MultiFileSourceTable.ofSingle(schema, src)

        val iter = table.vectorBlocks(1024)
        assertEquals(2, iter.schema().fieldsCount)
        assertTrue(iter.hasNext())

        val block = iter.next()
        assertEquals(2, block.vectorSize)
        assertFalse(iter.hasNext())
    }

    @Test
    fun shouldProduceEmptyIterator_whenNoSources() {
        val table = MultiFileSourceTable.empty(schema)
        val iter = table.vectorBlocks()
        assertFalse(iter.hasNext())
    }

    @Test
    fun shouldProduceMultipleBatches_whenBatchSizeSmall() {
        val records = (1..5).map { Record.of("id" to it, "name" to "user-$it") }
        val src = InMemoryRecordSource(schema, records)
        val table = MultiFileSourceTable.ofSingle(schema, src)

        val iter = table.vectorBlocks(2)
        val blocks = mutableListOf<io.qpointz.mill.proto.VectorBlock>()
        while (iter.hasNext()) blocks.add(iter.next())

        assertEquals(3, blocks.size) // 2 + 2 + 1
        assertEquals(2, blocks[0].vectorSize)
        assertEquals(2, blocks[1].vectorSize)
        assertEquals(1, blocks[2].vectorSize)
    }

    @Test
    fun shouldPreserveFieldValues_inVectorBlocks() {
        val src = makeSource(Record.of("id" to 42, "name" to "Alice"))
        val table = MultiFileSourceTable.ofSingle(schema, src)

        val block = table.vectorBlocks().next()
        assertEquals(1, block.vectorSize)

        // Find id vector (fieldIdx=0)
        val idVector = block.vectorsList.first { it.fieldIdx == 0 }
        assertEquals(42, idVector.i32Vector.getValues(0))

        // Find name vector (fieldIdx=1)
        val nameVector = block.vectorsList.first { it.fieldIdx == 1 }
        assertEquals("Alice", nameVector.stringVector.getValues(0))
    }

    @Test
    fun shouldHandleNullValues_inVectorBlocks() {
        val src = makeSource(Record.of("id" to 1, "name" to null))
        val table = MultiFileSourceTable.ofSingle(schema, src)

        val block = table.vectorBlocks().next()
        val nameVector = block.vectorsList.first { it.fieldIdx == 1 }
        assertTrue(nameVector.hasNulls())
        assertTrue(nameVector.nulls.getNulls(0))
    }

    @Test
    fun shouldSupportMultipleIterations_overRecords() {
        val src = makeSource(Record.of("id" to 1, "name" to "Alice"))
        val table = MultiFileSourceTable.ofSingle(schema, src)

        // Note: each call to records() creates a new iteration over the sources,
        // but InMemoryRecordSource supports multiple iterations
        assertEquals(1, table.records().toList().size)
        assertEquals(1, table.records().toList().size)
    }

    @Test
    fun shouldConcatenateVectorBlocks_fromMultipleSources() {
        val src1 = makeSource(Record.of("id" to 1, "name" to "Alice"))
        val src2 = makeSource(Record.of("id" to 2, "name" to "Bob"))

        val table = MultiFileSourceTable(schema, listOf(src1, src2))
        val iter = table.vectorBlocks(1024)

        val blocks = mutableListOf<io.qpointz.mill.proto.VectorBlock>()
        while (iter.hasNext()) blocks.add(iter.next())

        // Each source produces one block
        assertEquals(2, blocks.size)
        assertEquals(1, blocks[0].vectorSize)
        assertEquals(1, blocks[1].vectorSize)
    }
}
