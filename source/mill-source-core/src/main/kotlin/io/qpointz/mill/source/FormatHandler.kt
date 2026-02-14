package io.qpointz.mill.source

/**
 * Creates a [RecordSource] from a single blob.
 *
 * Each file format (CSV, Excel, Parquet, etc.) provides its own
 * [FormatHandler] implementation that knows how to infer the schema
 * and create the appropriate [FlowRecordSource] or [FlowVectorSource].
 *
 * @see RecordSource
 */
interface FormatHandler {

    /**
     * Infers the [RecordSchema] from the given blob without reading all data.
     *
     * @param blob       the blob to inspect
     * @param blobSource the source providing I/O access to the blob
     * @return the inferred schema
     */
    fun inferSchema(blob: BlobPath, blobSource: BlobSource): RecordSchema

    /**
     * Creates a [RecordSource] that reads data from the given blob.
     *
     * The returned source may be a [FlowRecordSource] (row-oriented) or
     * [FlowVectorSource] (columnar), depending on the format.
     *
     * @param blob       the blob to read
     * @param blobSource the source providing I/O access to the blob
     * @param schema     the schema to use (typically from [inferSchema])
     * @return a record source for the blob
     */
    fun createRecordSource(blob: BlobPath, blobSource: BlobSource, schema: RecordSchema): RecordSource
}
