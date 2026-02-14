package io.qpointz.mill.source.format.text

import com.univocity.parsers.tsv.TsvParser
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
    private val inputStream: InputStream,
    override val schema: RecordSchema,
    private val settings: TsvSettings = TsvSettings()
) : FlowRecordSource {

    override fun iterator(): Iterator<Record> {
        val parserSettings = settings.toParserSettings()
        val parser = TsvParser(parserSettings)
        val reader = InputStreamReader(inputStream, Charsets.UTF_8)
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
) : Iterator<Record> {

    private var nextRow: Array<String?>? = parser.parseNext()

    override fun hasNext(): Boolean {
        if (nextRow == null) {
            parser.stopParsing()
        }
        return nextRow != null
    }

    override fun next(): Record {
        val row = nextRow ?: run {
            parser.stopParsing()
            throw NoSuchElementException()
        }
        val record = toRecord(row, schema)
        nextRow = parser.parseNext()
        return record
    }
}
