package io.qpointz.mill.source.format.text

import com.univocity.parsers.csv.CsvParser
import io.qpointz.mill.source.*
import io.qpointz.mill.types.sql.DatabaseType
import java.io.InputStreamReader

/**
 * [FormatHandler] for delimited text files (CSV).
 *
 * Infers schema by reading the header row (or generating column names)
 * and creates row-oriented [CsvRecordSource] instances for data reading.
 * All columns are typed as nullable [DatabaseType.string].
 *
 * @property settings CSV parsing configuration
 */
class CsvFormatHandler(
    private val settings: CsvSettings = CsvSettings()
) : FormatHandler {

    /**
     * Infers the Mill [RecordSchema] from the CSV file.
     *
     * If [CsvSettings.hasHeader] is `true`, the first row is read as column names.
     * Otherwise, column names are generated as `col_0`, `col_1`, etc. based on
     * the first data row.
     */
    override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema {
        val inputStream = blobSource.openInputStream(blob)
        return inputStream.use { stream ->
            val parserSettings = settings.toParserSettings()
            val parser = CsvParser(parserSettings)
            val reader = InputStreamReader(stream, Charsets.UTF_8)
            parser.beginParsing(reader)

            val firstRow = parser.parseNext()
            parser.stopParsing()

            if (firstRow == null) {
                return@use RecordSchema.empty()
            }

            val columnNames = if (settings.hasHeader) {
                firstRow.map { it?.trim() ?: "" }
            } else {
                firstRow.indices.map { "col_$it" }
            }

            val fields = columnNames.mapIndexed { idx, name ->
                SchemaField(name, idx, DatabaseType.string(true, -1))
            }
            RecordSchema(fields)
        }
    }

    /**
     * Creates a [CsvRecordSource] for the given blob.
     */
    override fun createRecordSource(blob: BlobPath, blobSource: BlobSource, schema: RecordSchema): RecordSource {
        val inputStream = blobSource.openInputStream(blob)
        return CsvRecordSource(inputStream, schema, settings)
    }
}
