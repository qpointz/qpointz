package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.LocalBlobPath
import io.qpointz.mill.source.LocalBlobSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class TextLineRecordStatisticReaderTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun shouldSubtractHeaderRow_whenHasHeader() {
        writeBlob("data.csv", "id,name\n1,Alice\n2,Bob\n")

        val count = readLineStatistic(hasHeader = true)
        assertEquals(2L, count)
    }

    @Test
    fun shouldCountAllLines_whenNoHeader() {
        writeBlob("data.csv", "1,Alice\n2,Bob\n")

        val count = readLineStatistic(hasHeader = false)
        assertEquals(2L, count)
    }

    @Test
    fun shouldUseConfiguredLineSeparator() {
        writeBlob("data.csv", "1|2|3|4")

        val count = TextLineRecordStatisticReader.readRecordStatistic(
            blob = blob("data.csv"),
            blobSource = LocalBlobSource(tempDir),
            lineSeparator = "|",
            hasHeader = false,
        ).estimatedRowCount

        assertEquals(4L, count)
    }

    @Test
    fun shouldDefaultLineSeparatorToLf() {
        assertEquals("\n", TextLineRecordStatisticReader.effectiveLineSeparator(null))
        assertEquals("\n", TextLineRecordStatisticReader.effectiveLineSeparator(""))
    }

    private fun readLineStatistic(hasHeader: Boolean): Long? =
        TextLineRecordStatisticReader.readRecordStatistic(
            blob = blob("data.csv"),
            blobSource = LocalBlobSource(tempDir),
            lineSeparator = "\n",
            hasHeader = hasHeader,
        ).estimatedRowCount

    private fun writeBlob(name: String, content: String) {
        Files.writeString(tempDir.resolve(name), content, Charsets.UTF_8)
    }

    private fun blob(name: String) =
        LocalBlobPath.of(tempDir, tempDir.resolve(name))
}
