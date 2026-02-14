package io.qpointz.mill.source

import io.qpointz.mill.proto.VectorBlock
import io.qpointz.mill.types.sql.DatabaseType
import io.qpointz.mill.vectors.VectorBlockIterator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BridgesTest {

    private val schema = RecordSchema.of(
        "id" to DatabaseType.i32(false),
        "name" to DatabaseType.string(true, -1)
    )

    private fun sampleRecordSource(): InMemoryRecordSource =
        InMemoryRecordSource.of(
            schema,
            Record.of("id" to 1, "name" to "Alice"),
            Record.of("id" to 2, "name" to "Bob"),
            Record.of("id" to 3, "name" to "Charlie")
        )

    private fun sampleVectorSource(): FlowVectorSource {
        // Build a FlowVectorSource from records via the bridge,
        // then use it as test input for the reverse bridge
        val records = sampleRecordSource()
        return records.asVectorSource(1024)
    }

    // --- FlowRecordSource.asVectorSource ---

    @Test
    fun shouldConvertRecordSource_toVectorSource() {
        val vectorSource = sampleRecordSource().asVectorSource()
        assertEquals(schema, vectorSource.schema)
    }

    @Test
    fun shouldProduceVectorBlocks_fromRecordSource() {
        val vectorSource = sampleRecordSource().asVectorSource(1024)
        val iter = vectorSource.vectorBlocks()
        assertTrue(iter.hasNext())

        val block = iter.next()
        assertEquals(3, block.vectorSize)
        assertFalse(iter.hasNext())
    }

    @Test
    fun shouldPreserveValues_whenBridgingToVectors() {
        val vectorSource = sampleRecordSource().asVectorSource(1024)
        val block = vectorSource.vectorBlocks().next()

        val idVector = block.vectorsList.first { it.fieldIdx == 0 }
        assertEquals(1, idVector.i32Vector.getValues(0))
        assertEquals(2, idVector.i32Vector.getValues(1))
        assertEquals(3, idVector.i32Vector.getValues(2))

        val nameVector = block.vectorsList.first { it.fieldIdx == 1 }
        assertEquals("Alice", nameVector.stringVector.getValues(0))
        assertEquals("Bob", nameVector.stringVector.getValues(1))
        assertEquals("Charlie", nameVector.stringVector.getValues(2))
    }

    @Test
    fun shouldRespectBatchSize_whenBridgingToVectors() {
        val vectorSource = sampleRecordSource().asVectorSource()
        val iter = vectorSource.vectorBlocks(2)

        val blocks = mutableListOf<VectorBlock>()
        while (iter.hasNext()) blocks.add(iter.next())

        assertEquals(2, blocks.size) // 2 + 1
        assertEquals(2, blocks[0].vectorSize)
        assertEquals(1, blocks[1].vectorSize)
    }

    // --- FlowVectorSource.asRecordSource ---

    @Test
    fun shouldConvertVectorSource_toRecordSource() {
        val recordSource = sampleVectorSource().asRecordSource()
        assertEquals(schema, recordSource.schema)
    }

    @Test
    fun shouldProduceRecords_fromVectorSource() {
        val recordSource = sampleVectorSource().asRecordSource()
        val records = recordSource.toList()
        assertEquals(3, records.size)
    }

    @Test
    fun shouldPreserveValues_whenBridgingToRecords() {
        val recordSource = sampleVectorSource().asRecordSource()
        val records = recordSource.toList()

        assertEquals(1, records[0]["id"])
        assertEquals("Alice", records[0]["name"])
        assertEquals(2, records[1]["id"])
        assertEquals("Bob", records[1]["name"])
        assertEquals(3, records[2]["id"])
        assertEquals("Charlie", records[2]["name"])
    }

    // --- Round-trip ---

    @Test
    fun shouldRoundTrip_recordToVectorToRecord() {
        val original = sampleRecordSource()
        val roundTripped = original.asVectorSource(1024).asRecordSource().toList()

        assertEquals(3, roundTripped.size)
        assertEquals(1, roundTripped[0]["id"])
        assertEquals("Alice", roundTripped[0]["name"])
        assertEquals(2, roundTripped[1]["id"])
        assertEquals("Bob", roundTripped[1]["name"])
        assertEquals(3, roundTripped[2]["id"])
        assertEquals("Charlie", roundTripped[2]["name"])
    }

    @Test
    fun shouldHandleEmptySource_inBothDirections() {
        val empty = InMemoryRecordSource.empty(schema)

        // Record -> Vector
        val vectorSource = empty.asVectorSource()
        assertFalse(vectorSource.vectorBlocks().hasNext())

        // Vector -> Record (using the empty vector source)
        val recordSource = vectorSource.asRecordSource()
        assertEquals(0, recordSource.toList().size)
    }

    @Test
    fun shouldHandleNullValues_inRoundTrip() {
        val source = InMemoryRecordSource.of(
            schema,
            Record.of("id" to 1, "name" to null)
        )

        val roundTripped = source.asVectorSource(1024).asRecordSource().toList()
        assertEquals(1, roundTripped.size)
        assertEquals(1, roundTripped[0]["id"])
        assertNull(roundTripped[0]["name"])
    }

    @Test
    fun shouldHandleMultipleTypes_inRoundTrip() {
        val multiSchema = RecordSchema.of(
            "bool" to DatabaseType.bool(false),
            "i32" to DatabaseType.i32(false),
            "i64" to DatabaseType.i64(false),
            "fp32" to DatabaseType.fp32(false, -1, -1),
            "fp64" to DatabaseType.fp64(false, -1, -1),
            "str" to DatabaseType.string(true, -1)
        )

        val source = InMemoryRecordSource.of(
            multiSchema,
            Record.of(
                "bool" to true,
                "i32" to 42,
                "i64" to 100L,
                "fp32" to 3.14f,
                "fp64" to 2.718,
                "str" to "hello"
            )
        )

        val roundTripped = source.asVectorSource(1024).asRecordSource().toList()
        assertEquals(1, roundTripped.size)
        val r = roundTripped[0]
        assertEquals(true, r["bool"])
        assertEquals(42, r["i32"])
        assertEquals(100L, r["i64"])
        assertEquals(3.14f, r["fp32"])
        assertEquals(2.718, r["fp64"])
        assertEquals("hello", r["str"])
    }
}
