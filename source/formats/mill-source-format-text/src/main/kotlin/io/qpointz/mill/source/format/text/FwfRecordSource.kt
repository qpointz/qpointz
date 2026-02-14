package io.qpointz.mill.source.format.text

import com.univocity.parsers.fixed.FixedWidthParser
import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Row-oriented record source that reads a single fixed-width file using
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * All values are read as strings. The input stream is consumed lazily
 * during iteration and closed when the iterator is exhausted.
 *
 * @property inputStream the fixed-width file input stream
 * @property schema      the Mill schema describing the expected fields
 * @property settings    FWF parsing configuration with column positions
 */
class FwfRecordSource(
    private val inputStream: InputStream,
    override val schema: RecordSchema,
    private val settings: FwfSettings
) : FlowRecordSource {

    override fun iterator(): Iterator<Record> {
        val parserSettings = settings.toParserSettings()
        val parser = FixedWidthParser(parserSettings)
        val reader = InputStreamReader(inputStream, Charsets.UTF_8)
        parser.beginParsing(reader)

        // Skip header row if present — schema is already known
        if (settings.hasHeader) {
            parser.parseNext()
        }

        return FwfRecordIterator(parser, schema)
    }
}

/**
 * Lazy iterator over fixed-width records produced by a [FixedWidthParser].
 */
internal class FwfRecordIterator(
    private val parser: FixedWidthParser,
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
