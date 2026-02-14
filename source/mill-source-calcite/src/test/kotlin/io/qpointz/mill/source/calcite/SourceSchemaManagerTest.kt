package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.*
import io.qpointz.mill.source.descriptor.ConflictResolution
import io.qpointz.mill.source.factory.MaterializedSource
import io.qpointz.mill.types.sql.DatabaseType
import io.qpointz.mill.vectors.VectorBlockIterator
import org.apache.calcite.schema.SchemaPlus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.InputStream
import java.nio.channels.SeekableByteChannel

class SourceSchemaManagerTest {

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

    private fun resolvedSource(name: String, tables: Map<String, SourceTable>): ResolvedSource {
        val materialized = MaterializedSource(
            name = name,
            blobSource = stubBlobSource(),
            readers = emptyList(),
            conflicts = ConflictResolution.DEFAULT
        )
        return ResolvedSource(materialized, tables)
    }

    @Test
    fun shouldStartEmpty() {
        val manager = SourceSchemaManager()
        assertTrue(manager.isEmpty())
        assertEquals(0, manager.size)
        assertTrue(manager.names().isEmpty())
        manager.close()
    }

    @Test
    fun shouldAddAndGetSchema() {
        val manager = SourceSchemaManager()
        val rs = resolvedSource("test", mapOf("users" to stubSourceTable("id", "name")))
        val schema = manager.add("test", rs)

        assertNotNull(schema)
        assertEquals(1, manager.size)
        assertFalse(manager.isEmpty())
        assertTrue(manager.names().contains("test"))
        assertSame(schema, manager.get("test"))

        manager.close()
    }

    @Test
    fun shouldReturnNullForUnknownName() {
        val manager = SourceSchemaManager()
        assertNull(manager.get("nonexistent"))
        manager.close()
    }

    @Test
    fun shouldAddMultipleSchemas() {
        val manager = SourceSchemaManager()
        manager.add("schema1", resolvedSource("s1", mapOf("t1" to stubSourceTable("a"))))
        manager.add("schema2", resolvedSource("s2", mapOf("t2" to stubSourceTable("b"))))
        manager.add("schema3", resolvedSource("s3", mapOf("t3" to stubSourceTable("c"))))

        assertEquals(3, manager.size)
        assertEquals(setOf("schema1", "schema2", "schema3"), manager.names())

        manager.close()
    }

    @Test
    fun shouldReplaceExistingSchema() {
        val manager = SourceSchemaManager()
        val rs1 = resolvedSource("test", mapOf("users" to stubSourceTable("id")))
        val schema1 = manager.add("test", rs1)

        val rs2 = resolvedSource("test", mapOf("orders" to stubSourceTable("order_id")))
        val schema2 = manager.add("test", rs2)

        assertEquals(1, manager.size)
        assertSame(schema2, manager.get("test"))
        assertNotSame(schema1, schema2)

        // New schema should have "orders" table, not "users"
        assertTrue(schema2.flowTables().containsKey("orders"))
        assertFalse(schema2.flowTables().containsKey("users"))

        manager.close()
    }

    @Test
    fun shouldRemoveSchema() {
        val manager = SourceSchemaManager()
        manager.add("test", resolvedSource("test", mapOf("t" to stubSourceTable("a"))))

        assertTrue(manager.remove("test"))
        assertEquals(0, manager.size)
        assertNull(manager.get("test"))

        // Removing non-existent should return false
        assertFalse(manager.remove("test"))

        manager.close()
    }

    @Test
    fun shouldRemoveNonExistent() {
        val manager = SourceSchemaManager()
        assertFalse(manager.remove("ghost"))
        manager.close()
    }

    @Test
    fun shouldClearOnClose() {
        val manager = SourceSchemaManager()
        manager.add("s1", resolvedSource("s1", mapOf("t1" to stubSourceTable("a"))))
        manager.add("s2", resolvedSource("s2", mapOf("t2" to stubSourceTable("b"))))

        assertEquals(2, manager.size)
        manager.close()
        assertEquals(0, manager.size)
        assertTrue(manager.isEmpty())
    }

    @Test
    fun shouldRegisterAllIntoCalciteRootSchema() {
        val manager = SourceSchemaManager()
        manager.add("alpha", resolvedSource("alpha", mapOf("t1" to stubSourceTable("a"))))
        manager.add("beta", resolvedSource("beta", mapOf("t2" to stubSourceTable("b"))))

        val rootSchema = Mockito.mock(SchemaPlus::class.java)
        manager.registerAll(rootSchema)

        // Verify both schemas were registered
        Mockito.verify(rootSchema).add(Mockito.eq("alpha"), Mockito.any(FlowSchema::class.java))
        Mockito.verify(rootSchema).add(Mockito.eq("beta"), Mockito.any(FlowSchema::class.java))
        Mockito.verifyNoMoreInteractions(rootSchema)

        manager.close()
    }

    @Test
    fun shouldRegisterNothingWhenEmpty() {
        val manager = SourceSchemaManager()
        val rootSchema = Mockito.mock(SchemaPlus::class.java)
        manager.registerAll(rootSchema)
        Mockito.verifyNoInteractions(rootSchema)
        manager.close()
    }

    @Test
    fun shouldExposeFlowTablesViaSchema() {
        val manager = SourceSchemaManager()
        val tables = mapOf(
            "users" to stubSourceTable("id", "name"),
            "orders" to stubSourceTable("order_id", "total")
        )
        manager.add("mydb", resolvedSource("mydb", tables))

        val schema = manager.get("mydb")!!
        val flowTables = schema.flowTables()
        assertEquals(2, flowTables.size)
        assertTrue(flowTables.containsKey("users"))
        assertTrue(flowTables.containsKey("orders"))

        manager.close()
    }
}
