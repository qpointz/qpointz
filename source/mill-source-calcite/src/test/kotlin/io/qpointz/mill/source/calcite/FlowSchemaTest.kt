package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.*
import io.qpointz.mill.source.descriptor.ConflictResolution
import io.qpointz.mill.source.factory.MaterializedSource
import io.qpointz.mill.types.sql.DatabaseType
import io.qpointz.mill.vectors.VectorBlockIterator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.nio.channels.SeekableByteChannel

class FlowSchemaTest {

    private fun stubSourceTable(vararg fieldNames: String): SourceTable {
        val schema = RecordSchema.of(
            *fieldNames.map { it to DatabaseType.string(true, -1) }.toTypedArray()
        )
        return object : SourceTable {
            override val schema: RecordSchema = schema
            override fun records(): Iterable<Record> = emptyList()
            override fun vectorBlocks(batchSize: Int): VectorBlockIterator {
                throw UnsupportedOperationException()
            }
        }
    }

    private fun stubBlobSource(): BlobSource = object : BlobSource {
        override fun listBlobs(): Sequence<BlobPath> = emptySequence()
        override fun openInputStream(path: BlobPath): InputStream =
            throw UnsupportedOperationException()
        override fun openSeekableChannel(path: BlobPath): SeekableByteChannel =
            throw UnsupportedOperationException()
        override fun close() {}
    }

    private fun resolvedSource(tables: Map<String, SourceTable>): ResolvedSource {
        val materialized = MaterializedSource(
            name = "test-source",
            blobSource = stubBlobSource(),
            readers = emptyList(),
            conflicts = ConflictResolution.DEFAULT
        )
        return ResolvedSource(materialized, tables)
    }

    @Test
    fun shouldExposeTablesFromResolvedSource() {
        val tables = mapOf(
            "users" to stubSourceTable("id", "name"),
            "orders" to stubSourceTable("order_id", "total")
        )
        val schema = FlowSchema(resolvedSource(tables))

        val tableMap = schema.flowTables()
        assertEquals(2, tableMap.size)
        assertTrue(tableMap.containsKey("users"))
        assertTrue(tableMap.containsKey("orders"))
        assertTrue(tableMap["users"] is FlowTable)
        assertTrue(tableMap["orders"] is FlowTable)
    }

    @Test
    fun shouldReturnEmptyTableMap_whenNoTables() {
        val schema = FlowSchema(resolvedSource(emptyMap()))
        assertTrue(schema.flowTables().isEmpty())
    }

    @Test
    fun shouldExposeResolvedSource() {
        val rs = resolvedSource(emptyMap())
        val schema = FlowSchema(rs)
        assertSame(rs, schema.resolvedSource())
    }
}
