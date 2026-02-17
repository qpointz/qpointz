package io.qpointz.mill.source.format.text

import com.univocity.parsers.csv.CsvWriter
import io.qpointz.mill.source.FlowRecordWriter
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * Writes Mill [Record] instances as a CSV file using
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * @property schema       the schema defining column names and order
 * @property settings     CSV formatting configuration
 * @property outputStream the destination for CSV output
 */
class CsvRecordWriter(
    private val schema: RecordSchema,
    private val settings: CsvSettings = CsvSettings(),
    private val outputStream: OutputStream
) : FlowRecordWriter {

    private var csvWriter: CsvWriter? = null

    override fun open() {
        val writerSettings = settings.toWriterSettings()
        val writer = CsvWriter(OutputStreamWriter(outputStream, Charsets.UTF_8), writerSettings)

        if (settings.hasHeader) {
            writer.writeHeaders(schema.fieldNames)
        }

        csvWriter = writer
    }

    override fun write(record: Record) {
        val writer = csvWriter ?: throw IllegalStateException("Writer not opened. Call open() first.")
        val row = schema.fields.map { field ->
            record[field.name]?.toString()
        }.toTypedArray()
        writer.writeRow(*row)
    }

    override fun close() {
        csvWriter?.close()
        csvWriter = null
    }
}
