package io.qpointz.mill.source.format.text

import com.univocity.parsers.csv.CsvParser
import io.qpointz.mill.source.CloseableRecordIterator
import io.qpointz.mill.source.FlowRecordSource
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Row-oriented record source that reads a single CSV file using
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * All values are read as strings — no type coercion is applied during parsing.
 * The input stream is consumed lazily during iteration and closed when
 * the iterator is exhausted.
 *
 * @property inputStream the CSV file input stream
 * @property schema      the Mill schema describing the expected fields
 * @property settings    CSV parsing configuration
 */
class CsvRecordSource(
    private val inputStreamSupplier: () -> InputStream,
    override val schema: RecordSchema,
    private val settings: CsvSettings = CsvSettings()
) : FlowRecordSource {

    constructor(
        inputStream: InputStream,
        schema: RecordSchema,
        settings: CsvSettings = CsvSettings()
    ) : this({ inputStream }, schema, settings)

    override fun iterator(): Iterator<Record> {
        val parserSettings = settings.toParserSettings()
        val parser = CsvParser(parserSettings)
        val reader = InputStreamReader(inputStreamSupplier(), Charsets.UTF_8)
        parser.beginParsing(reader)

        // Skip header row if present — schema is already known
        if (settings.hasHeader) {
            parser.parseNext()
        }

        return CsvRecordIterator(parser, schema)
    }
}

/**
 * Lazy iterator over CSV records produced by a [CsvParser].
 */
internal class CsvRecordIterator(
    private val parser: CsvParser,
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

/**
 * Converts a parsed row array to a [Record] using the given schema.
 */
internal fun toRecord(row: Array<String?>, schema: RecordSchema): Record {
    val values = schema.fields.associate { field ->
        val value = if (field.index < row.size) row[field.index] else null
        field.name to value
    }
    return Record(values)
}
