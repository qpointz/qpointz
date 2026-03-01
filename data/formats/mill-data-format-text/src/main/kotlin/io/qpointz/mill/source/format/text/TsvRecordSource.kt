package io.qpointz.mill.source.format.text

import com.univocity.parsers.tsv.TsvParser
import io.qpointz.mill.source.CloseableRecordIterator
import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Row-oriented record source that reads a single TSV file using
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * TSV uses escape sequences instead of quoting — this is distinct from
 * CSV with a tab delimiter.
 *
 * @property inputStream the TSV file input stream
 * @property schema      the Mill schema describing the expected fields
 * @property settings    TSV parsing configuration
 */
class TsvRecordSource(
    private val inputStreamSupplier: () -> InputStream,
    override val schema: RecordSchema,
    private val settings: TsvSettings = TsvSettings()
) : FlowRecordSource {

    constructor(
        inputStream: InputStream,
        schema: RecordSchema,
        settings: TsvSettings = TsvSettings()
    ) : this({ inputStream }, schema, settings)

    override fun iterator(): Iterator<Record> {
        val parserSettings = settings.toParserSettings()
        val parser = TsvParser(parserSettings)
        val reader = InputStreamReader(inputStreamSupplier(), Charsets.UTF_8)
        parser.beginParsing(reader)

        // Skip header row if present — schema is already known
        if (settings.hasHeader) {
            parser.parseNext()
        }

        return TsvRecordIterator(parser, schema)
    }
}

/**
 * Lazy iterator over TSV records produced by a [TsvParser].
 */
internal class TsvRecordIterator(
    private val parser: TsvParser,
    private val schema: RecordSchema
) : CloseableRecordIterator {

    private var nextRow: Array<String?>? = parser.parseNext()
    private var closed = false

    override fun hasNext(): Boolean {
        if (nextRow == null) {
            close()
        }
        return nextRow != null
    }

    override fun next(): Record {
        val row = nextRow ?: run {
            close()
            throw NoSuchElementException()
        }
        val record = toRecord(row, schema)
        nextRow = parser.parseNext()
        return record
    }

    override fun close() {
        if (closed) return
        closed = true
        parser.stopParsing()
    }
}
