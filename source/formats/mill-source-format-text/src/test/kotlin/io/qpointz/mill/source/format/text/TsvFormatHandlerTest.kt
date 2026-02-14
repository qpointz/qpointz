package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.LocalBlobSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class TsvFormatHandlerTest {

    @TempDir
    lateinit var tempDir: Path

    private val tsvContent = "id\tname\tscore\n1\tAlice\t95.5\n2\tBob\t82.0\n3\tCharlie\t77.3"

    @Test
    fun shouldInferSchemaFromTsvFile() {
        CsvTestUtils.writeCsvFile(tempDir, "test.tsv", tsvContent)

        val handler = TsvFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()
        assertEquals(1, blobs.size)

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(3, schema.size)
        assertEquals("id", schema.fields[0].name)
        assertEquals("name", schema.fields[1].name)
        assertEquals("score", schema.fields[2].name)
    }

    @Test
    fun shouldInferSchemaWithoutHeader() {
        val noHeaderTsv = "1\tAlice\t95.5\n2\tBob\t82.0"
        CsvTestUtils.writeCsvFile(tempDir, "test.tsv", noHeaderTsv)

        val handler = TsvFormatHandler(TsvSettings(hasHeader = false))
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(3, schema.size)
        assertEquals("col_0", schema.fields[0].name)
        assertEquals("col_1", schema.fields[1].name)
    }

    @Test
    fun shouldCreateRecordSourceFromTsvFile() {
        CsvTestUtils.writeCsvFile(tempDir, "test.tsv", tsvContent)

        val handler = TsvFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        val source = handler.createRecordSource(blobs[0], blobSource, schema)

        assertTrue(source is FlowRecordSource)
        val result = (source as FlowRecordSource).toList()

        assertEquals(3, result.size)
        assertEquals("1", result[0]["id"])
        assertEquals("Alice", result[0]["name"])
        assertEquals("Charlie", result[2]["name"])
    }

    @Test
    fun shouldHandleEmptyTsvFile() {
        CsvTestUtils.writeCsvFile(tempDir, "empty.tsv", "id\tname\n")

        val handler = TsvFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(2, schema.size)

        val source = handler.createRecordSource(blobs[0], blobSource, schema) as FlowRecordSource
        assertTrue(source.toList().isEmpty())
    }
}
