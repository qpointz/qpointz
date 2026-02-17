package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.LocalBlobSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CsvFormatHandlerTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun shouldInferSchemaFromCsvFile() {
        CsvTestUtils.writeCsvFile(tempDir, "test.csv", CsvTestUtils.SIMPLE_CSV)

        val handler = CsvFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()
        assertEquals(1, blobs.size)

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(4, schema.size)
        assertEquals("id", schema.fields[0].name)
        assertEquals("name", schema.fields[1].name)
        assertEquals("score", schema.fields[2].name)
        assertEquals("active", schema.fields[3].name)
    }

    @Test
    fun shouldInferSchemaWithoutHeader() {
        CsvTestUtils.writeCsvFile(tempDir, "test.csv", CsvTestUtils.NO_HEADER_CSV)

        val handler = CsvFormatHandler(CsvSettings(hasHeader = false))
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(4, schema.size)
        assertEquals("col_0", schema.fields[0].name)
        assertEquals("col_1", schema.fields[1].name)
    }

    @Test
    fun shouldCreateRecordSourceFromCsvFile() {
        CsvTestUtils.writeCsvFile(tempDir, "test.csv", CsvTestUtils.SIMPLE_CSV)

        val handler = CsvFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        val source = handler.createRecordSource(blobs[0], blobSource, schema)

        assertTrue(source is FlowRecordSource)
        val result = (source as FlowRecordSource).toList()

        assertEquals(3, result.size)
        assertEquals("1", result[0]["id"])
        assertEquals("Alice", result[0]["name"])
        assertNull(result[1]["name"])
        assertEquals("Charlie", result[2]["name"])
    }

    @Test
    fun shouldHandleEmptyCsvFile() {
        CsvTestUtils.writeCsvFile(tempDir, "empty.csv", "id,name\n")

        val handler = CsvFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(2, schema.size)

        val source = handler.createRecordSource(blobs[0], blobSource, schema) as FlowRecordSource
        assertTrue(source.toList().isEmpty())
    }

    @Test
    fun shouldHandleTsvFile() {
        CsvTestUtils.writeCsvFile(tempDir, "test.tsv", CsvTestUtils.SIMPLE_TSV)

        val handler = CsvFormatHandler(CsvSettings(delimiter = '\t'))
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(3, schema.size)
        assertEquals("id", schema.fields[0].name)

        val source = handler.createRecordSource(blobs[0], blobSource, schema) as FlowRecordSource
        val result = source.toList()
        assertEquals(3, result.size)
        assertEquals("Alice", result[0]["name"])
    }
}
