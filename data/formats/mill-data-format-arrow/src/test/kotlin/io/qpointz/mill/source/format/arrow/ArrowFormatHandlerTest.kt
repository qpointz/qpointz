package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.LocalBlobSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class ArrowFormatHandlerTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun shouldInferSchemaAndReadArrowStream() {
        val file = tempDir.resolve("events.arrow")
        ArrowTestUtils.writeArrowStream(file)

        val handler = ArrowFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blob = blobSource.listBlobs().first()

        val schema = handler.inferSchema(blob, blobSource)
        assertEquals(4, schema.size)

        val source = handler.createRecordSource(blob, blobSource, schema)
        assertTrue(source is FlowRecordSource)
        val records = (source as FlowRecordSource).toList()
        assertEquals(2, records.size)
        assertEquals("Alice", records[0]["name"])
    }

    @Test
    fun shouldInferSchemaAndReadArrowFile() {
        val file = tempDir.resolve("events-ipc-file.arrow")
        ArrowTestUtils.writeArrowFile(file)

        val handler = ArrowFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blob = blobSource.listBlobs().first { it.uri.path.endsWith("events-ipc-file.arrow") }

        val schema = handler.inferSchema(blob, blobSource)
        val source = handler.createRecordSource(blob, blobSource, schema) as FlowRecordSource
        val records = source.toList()
        assertEquals(2, records.size)
        assertEquals(1, records[0]["id"])
    }
}
