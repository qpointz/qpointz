package io.qpointz.mill.source.format.text

import com.univocity.parsers.tsv.TsvWriter
import io.qpointz.mill.source.FlowRecordWriter
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * Writes Mill [Record] instances as a TSV file using
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * @property schema       the schema defining column names and order
 * @property settings     TSV formatting configuration
 * @property outputStream the destination for TSV output
 */
class TsvRecordWriter(
    private val schema: RecordSchema,
    private val settings: TsvSettings = TsvSettings(),
    private val outputStream: OutputStream
) : FlowRecordWriter {

    private var tsvWriter: TsvWriter? = null

    override fun open() {
        val writerSettings = settings.toWriterSettings()
        val writer = TsvWriter(OutputStreamWriter(outputStream, Charsets.UTF_8), writerSettings)

        if (settings.hasHeader) {
            writer.writeHeaders(schema.fieldNames)
        }

        tsvWriter = writer
    }

    override fun write(record: Record) {
        val writer = tsvWriter ?: throw IllegalStateException("Writer not opened. Call open() first.")
        val row = schema.fields.map { field ->
            record[field.name]?.toString()
        }.toTypedArray()
        writer.writeRow(*row)
    }

    override fun close() {
        tsvWriter?.close()
        tsvWriter = null
    }
}
