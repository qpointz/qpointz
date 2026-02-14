package io.qpointz.mill.source.format.text

import com.univocity.parsers.fixed.FixedWidthWriter
import io.qpointz.mill.source.FlowRecordWriter
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * Writes Mill [Record] instances as a fixed-width text file using
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * @property schema       the schema defining column names and order
 * @property settings     FWF column definitions and configuration
 * @property outputStream the destination for output
 */
class FwfRecordWriter(
    private val schema: RecordSchema,
    private val settings: FwfSettings,
    private val outputStream: OutputStream
) : FlowRecordWriter {

    private var fwfWriter: FixedWidthWriter? = null

    override fun open() {
        val writerSettings = settings.toWriterSettings()
        val writer = FixedWidthWriter(OutputStreamWriter(outputStream, Charsets.UTF_8), writerSettings)

        if (settings.hasHeader) {
            writer.writeHeaders(settings.columns.map { it.name })
        }

        fwfWriter = writer
    }

    override fun write(record: Record) {
        val writer = fwfWriter ?: throw IllegalStateException("Writer not opened. Call open() first.")
        val row = schema.fields.map { field ->
            record[field.name]?.toString()
        }.toTypedArray()
        writer.writeRow(*row)
    }

    override fun close() {
        fwfWriter?.close()
        fwfWriter = null
    }
}
