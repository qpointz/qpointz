package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.LocalBlobSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class ArrowRecordWriterTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun shouldRoundTripRecordsThroughArrowStreamWriter() {
        val path = tempDir.resolve("roundtrip.arrow")
        ArrowTestUtils.writeArrowStream(path, ArrowTestUtils.testSchema, ArrowTestUtils.testRecords())

        val blobSource = LocalBlobSource(tempDir)
        val blob = blobSource.listBlobs().first()
        val schema = ArrowFormatHandler().inferSchema(blob, blobSource)
        val source = ArrowFormatHandler().createRecordSource(blob, blobSource, schema) as FlowRecordSource
        val result = source.toList()

        assertEquals(2, result.size)
        assertEquals(2, result[1]["id"])
    }
}
