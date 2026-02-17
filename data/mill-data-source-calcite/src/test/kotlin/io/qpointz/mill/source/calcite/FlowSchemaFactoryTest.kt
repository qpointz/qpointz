package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.*
import io.qpointz.mill.source.descriptor.ConflictResolution
import io.qpointz.mill.source.factory.MaterializedSource
import io.qpointz.mill.types.sql.DatabaseType
import io.qpointz.mill.vectors.VectorBlockIterator
import org.apache.calcite.schema.SchemaPlus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito
import java.io.InputStream
import java.nio.channels.SeekableByteChannel
import java.nio.file.Path

class FlowSchemaFactoryTest {

    private val mockParent: SchemaPlus = Mockito.mock(SchemaPlus::class.java)

    @Test
    fun shouldThrow_whenDescriptorFileMissing() {
        val factory = FlowSchemaFactory()
        val operand = mutableMapOf<String, Any>(
            "descriptorFile" to "/nonexistent/path/source.yaml"
        )
        assertThrows(IllegalArgumentException::class.java) {
            factory.create(mockParent, "test", operand)
        }
    }

    @Test
    fun shouldThrow_whenOperandMissingDescriptorFile() {
        val factory = FlowSchemaFactory()
        val operand = mutableMapOf<String, Any>()
        assertThrows(IllegalArgumentException::class.java) {
            factory.create(mockParent, "test", operand)
        }
    }

    @Test
    fun shouldThrow_whenDescriptorPathIsDirectory(@TempDir tempDir: Path) {
        val factory = FlowSchemaFactory()
        val operand = mutableMapOf<String, Any>(
            "descriptorFile" to tempDir.toString()
        )
        assertThrows(IllegalArgumentException::class.java) {
            factory.create(mockParent, "test", operand)
        }
    }

    @Test
    fun shouldThrow_whenDescriptorFileIsInvalidYaml(@TempDir tempDir: Path) {
        val badFile = tempDir.resolve("bad.yaml").toFile()
        badFile.writeText("this is not: [valid: yaml: for: source")

        val factory = FlowSchemaFactory()
        val operand = mutableMapOf<String, Any>(
            "descriptorFile" to badFile.absolutePath
        )
        assertThrows(IllegalStateException::class.java) {
            factory.create(mockParent, "test", operand)
        }
    }

    @Test
    fun shouldCreateSchema_fromResolvedSource() {
        val tables = mapOf(
            "users" to stubSourceTable("id", "name")
        )
        val resolved = resolvedSource(tables)
        val schema = FlowSchemaFactory.createSchema(resolved)

        assertNotNull(schema)
        assertEquals(1, schema.flowTables().size)
        assertTrue(schema.flowTables().containsKey("users"))
    }

    // --- helpers ---

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
}
