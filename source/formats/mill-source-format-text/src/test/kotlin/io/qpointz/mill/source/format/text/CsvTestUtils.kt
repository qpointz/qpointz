package io.qpointz.mill.source.format.text

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * Test utilities for creating in-memory CSV data.
 */
object CsvTestUtils {

    /** Standard CSV test content with header. */
    val SIMPLE_CSV = "id,name,score,active\n1,Alice,95.5,true\n2,,82.0,false\n3,Charlie,77.3,true"

    /** TSV test content with tab delimiter. */
    val SIMPLE_TSV = "id\tname\tscore\n1\tAlice\t95.5\n2\t\t82.0\n3\tCharlie\t77.3"

    /** CSV without header row. */
    val NO_HEADER_CSV = "1,Alice,95.5,true\n2,,82.0,false\n3,Charlie,77.3,true"

    /** CSV with quoted fields. */
    val QUOTED_CSV = "id,name,description\n1,Alice,\"Has a, comma\"\n2,Bob,\"Quoted \"\"word\"\"\"\n3,Charlie,Simple"

    /**
     * Creates an [InputStream] from a string.
     */
    fun toInputStream(content: String): InputStream =
        ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))

    /**
     * Writes a CSV file to a temp directory.
     */
    fun writeCsvFile(dir: Path, filename: String, content: String): Path {
        val filePath = dir.resolve(filename)
        Files.writeString(filePath, content, Charsets.UTF_8)
        return filePath
    }
}
