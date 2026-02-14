package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.LocalBlobSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class FwfFormatHandlerTest {

    @TempDir
    lateinit var tempDir: Path

    private val columns = listOf(
        FwfColumnDef("id", 0, 5),
        FwfColumnDef("name", 5, 20),
        FwfColumnDef("score", 20, 30)
    )

    private val settings = FwfSettings(columns = columns)

    private val fwfContent = """1    Alice          95.5      
2    Bob            82.0      
3    Charlie        77.3      """

    @Test
    fun shouldInferSchemaFromColumnDefs() {
        Files.writeString(tempDir.resolve("test.fwf"), fwfContent)

        val handler = FwfFormatHandler(settings)
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(3, schema.size)
        assertEquals("id", schema.fields[0].name)
        assertEquals("name", schema.fields[1].name)
        assertEquals("score", schema.fields[2].name)
    }

    @Test
    fun shouldCreateRecordSourceFromFwfFile() {
        Files.writeString(tempDir.resolve("test.fwf"), fwfContent)

        val handler = FwfFormatHandler(settings)
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        val source = handler.createRecordSource(blobs[0], blobSource, schema)

        assertTrue(source is FlowRecordSource)
        val result = (source as FlowRecordSource).toList()

        assertEquals(3, result.size)
        assertEquals("1", result[0]["id"])
        assertEquals("Alice", result[0]["name"])
        assertEquals("95.5", result[0]["score"])
    }
}
