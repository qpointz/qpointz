package io.qpointz.mill.source.format.avro

import io.qpointz.mill.source.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.InputStream
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.Path

class AvroFormatHandlerTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun shouldInferSchemaFromAvroFile() {
        val records = AvroTestUtils.createTestRecords()
        AvroTestUtils.writeAvroFile(tempDir, "test.avro", AvroTestUtils.TEST_SCHEMA, records)

        val handler = AvroFormatHandler()
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
    fun shouldCreateRecordSourceFromAvroFile() {
        val records = AvroTestUtils.createTestRecords()
        AvroTestUtils.writeAvroFile(tempDir, "test.avro", AvroTestUtils.TEST_SCHEMA, records)

        val handler = AvroFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

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
    fun shouldHandleEmptyAvroFile() {
        AvroTestUtils.writeAvroFile(tempDir, "empty.avro", AvroTestUtils.TEST_SCHEMA, emptyList())

        val handler = AvroFormatHandler()
        val blobSource = LocalBlobSource(tempDir)
        val blobs = blobSource.listBlobs().toList()

        val schema = handler.inferSchema(blobs[0], blobSource)
        assertEquals(4, schema.size)

        val source = handler.createRecordSource(blobs[0], blobSource, schema) as FlowRecordSource
        assertTrue(source.toList().isEmpty())
    }
}
