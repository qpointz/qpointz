package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.statistics.RecordStatistic
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Heuristic record-count estimate for delimited / fixed-width text blobs.
 *
 * Counts physical lines using the configured [lineSeparator] from the format descriptor
 * (default `\n`). When [hasHeader] is true, one line is subtracted for the header row.
 *
 * This is approximate: quoted fields containing line breaks are not handled.
 */
internal object TextLineRecordStatisticReader {

    /**
     * Estimates row count from physical line count in [blob].
     */
    fun readRecordStatistic(
        blob: BlobPath,
        blobSource: BlobSource,
        lineSeparator: String?,
        hasHeader: Boolean,
        skipEmptyLines: Boolean = false,
    ): RecordStatistic {
        val separator = effectiveLineSeparator(lineSeparator)
        val physicalLines = blobSource.openInputStream(blob).use { input ->
            countPhysicalLines(input, separator, skipEmptyLines)
        }
        val estimatedRows = estimateDataRows(physicalLines, hasHeader)
        return RecordStatistic(estimatedRowCount = estimatedRows)
    }

    /**
     * Returns the line separator from format settings, or `\n` when unset.
     */
    fun effectiveLineSeparator(configured: String?): String =
        configured?.takeIf { it.isNotEmpty() } ?: "\n"

    private fun estimateDataRows(physicalLines: Long, hasHeader: Boolean): Long =
        when {
            physicalLines == 0L -> 0L
            hasHeader -> (physicalLines - 1).coerceAtLeast(0)
            else -> physicalLines
        }

    private fun countPhysicalLines(input: InputStream, separator: String, skipEmptyLines: Boolean): Long {
        if (separator == "\n" || separator == "\r\n") {
            return countLinesWithReadLine(input, skipEmptyLines)
        }
        return countLinesBySeparatorScan(input, separator, skipEmptyLines)
    }

    private fun countLinesWithReadLine(input: InputStream, skipEmptyLines: Boolean): Long {
        InputStreamReader(input, Charsets.UTF_8).buffered().use { reader ->
            var count = 0L
            while (true) {
                val line = reader.readLine() ?: break
                if (skipEmptyLines && line.isEmpty()) {
                    continue
                }
                count++
            }
            return count
        }
    }

    private fun countLinesBySeparatorScan(input: InputStream, separator: String, skipEmptyLines: Boolean): Long {
        if (separator.isEmpty()) {
            return 0L
        }
        val content = InputStreamReader(input, Charsets.UTF_8).readText()
        if (content.isEmpty()) {
            return 0L
        }
        var lines = 0L
        var start = 0
        while (start <= content.length) {
            val index = content.indexOf(separator, start)
            if (index < 0) {
                val tail = content.substring(start)
                if (!skipEmptyLines || tail.isNotEmpty()) {
                    lines++
                }
                break
            }
            val segment = content.substring(start, index)
            if (!skipEmptyLines || segment.isNotEmpty()) {
                lines++
            }
            start = index + separator.length
            if (start >= content.length) {
                break
            }
        }
        return lines
    }
}
