package io.qpointz.mill.source.format.text

import com.univocity.parsers.tsv.TsvParser
import io.qpointz.mill.source.*
import io.qpointz.mill.types.sql.DatabaseType
import java.io.InputStreamReader

/**
 * [FormatHandler] for tab-separated value (TSV) files, backed by
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * TSV differs from CSV in that it uses escape sequences (`\t`, `\n`, `\r`, `\\`)
 * instead of quoting. All columns are typed as nullable [DatabaseType.string].
 *
 * @property settings TSV parsing configuration
 */
class TsvFormatHandler(
    private val settings: TsvSettings = TsvSettings()
) : FormatHandler {

    /**
     * Infers the Mill [RecordSchema] from the TSV file.
     *
     * If [TsvSettings.hasHeader] is `true`, the first row is read as column names.
     * Otherwise, column names are generated as `col_0`, `col_1`, etc.
     */
    override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema {
        val inputStream = blobSource.openInputStream(blob)
        return inputStream.use { stream ->
            val parserSettings = settings.toParserSettings()
            val parser = TsvParser(parserSettings)
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
     * Creates a [TsvRecordSource] for the given blob.
     */
    override fun createRecordSource(blob: BlobPath, blobSource: BlobSource, schema: RecordSchema): RecordSource {
        val inputStream = blobSource.openInputStream(blob)
        return TsvRecordSource(inputStream, schema, settings)
    }
}
