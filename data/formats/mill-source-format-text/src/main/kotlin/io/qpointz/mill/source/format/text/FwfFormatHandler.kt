package io.qpointz.mill.source.format.text

import io.qpointz.mill.source.*
import io.qpointz.mill.types.sql.DatabaseType

/**
 * [FormatHandler] for fixed-width text files, backed by
 * [Univocity Parsers](https://github.com/uniVocity/univocity-parsers).
 *
 * Uses the column definitions in [FwfSettings] to extract fields by
 * character position. All columns are typed as nullable [DatabaseType.string].
 *
 * @property settings FWF parsing configuration with column positions
 */
class FwfFormatHandler(
    private val settings: FwfSettings
) : FormatHandler {

    /**
     * Infers the Mill [RecordSchema] from the FWF column definitions.
     *
     * Unlike CSV, the schema is derived entirely from [FwfSettings.columns] —
     * the file contents are not needed for schema inference.
     */
    override fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema {
        val fields = settings.columns.mapIndexed { idx, col ->
            SchemaField(col.name, idx, DatabaseType.string(true, -1))
        }
        return RecordSchema(fields)
    }

    /**
     * Creates a [FwfRecordSource] for the given blob.
     */
    override fun createRecordSource(blob: BlobPath, blobSource: BlobSource, schema: RecordSchema): RecordSource {
        val inputStream = blobSource.openInputStream(blob)
        return FwfRecordSource(inputStream, schema, settings)
    }
}
