package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.source.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class ParquetFormatHandlerTest {

    @TempDir
    lateinit var tempDir: Path

    /** Filter to only .parquet blobs (writer may create sidecar files) */
    private fun parquetBlobs(blobSource: BlobSource): List<BlobPath> =
        blobSource.listBlobs().filter { it.uri.path.endsWith(".parquet") }.toList()

    @Test
    fun shouldInferSchemaFromParquetFile() {
        val records = ParquetTestUtils.createTestRecords()
        ParquetTestUtils.writeParquetFile(tempDir, "test.parquet", records)

        val handler = ParquetFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = parquetBlobs(blobSource)
        assertEquals(1, blobs.size)

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(4, schema.size)
        assertEquals("id", schema.fields[0].name)
        assertEquals("name", schema.fields[1].name)
        assertEquals("score", schema.fields[2].name)
        assertEquals("active", schema.fields[3].name)
    }

    @Test
    fun shouldCreateRecordSourceFromParquetFile() {
        val records = ParquetTestUtils.createTestRecords()
        ParquetTestUtils.writeParquetFile(tempDir, "test.parquet", records)

        val handler = ParquetFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = parquetBlobs(blobSource)

        val schema = handler.inferSchema(blobs[0], blobSource)
        val source = handler.createRecordSource(blobs[0], blobSource, schema)

        assertTrue(source is FlowRecordSource)
        val flowSource = source as FlowRecordSource
        val result = flowSource.toList()

        assertEquals(3, result.size)
        assertEquals(1L, result[0]["id"])
        assertEquals("Alice", result[0]["name"])
        assertNull(result[1]["name"])
        assertEquals("Charlie", result[2]["name"])
    }

    @Test
    fun shouldHandleNumericValues() {
        val records = ParquetTestUtils.createTestRecords()
        ParquetTestUtils.writeParquetFile(tempDir, "test.parquet", records)

        val handler = ParquetFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = parquetBlobs(blobSource)

        val schema = handler.inferSchema(blobs[0], blobSource)
        val source = handler.createRecordSource(blobs[0], blobSource, schema) as FlowRecordSource
        val result = source.toList()

        assertEquals(95.5, result[0]["score"])
        assertEquals(82.0, result[1]["score"])
        assertEquals(true, result[0]["active"])
        assertEquals(false, result[1]["active"])
    }

    @Test
    fun shouldHandleEmptyParquetFile() {
        ParquetTestUtils.writeParquetFile(tempDir, "empty.parquet", emptyList())

        val handler = ParquetFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = parquetBlobs(blobSource)

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(4, schema.size)

        val source = handler.createRecordSource(blobs[0], blobSource, schema) as FlowRecordSource
        assertTrue(source.toList().isEmpty())
    }
}
