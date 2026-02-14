package io.qpointz.mill.source

import io.qpointz.mill.vectors.VectorBlockIterator

/**
 * Base interface for all record sources.
 *
 * A [RecordSource] represents a single data origin (e.g. one file, one sheet)
 * and exposes the [RecordSchema] describing its structure. Concrete subtypes
 * provide either row-oriented ([FlowRecordSource]) or columnar
 * ([FlowVectorSource]) access to the data.
 */
interface RecordSource {
    /** Schema describing the fields produced by this source. */
    val schema: RecordSchema
}

/**
 * Row-oriented record source — produces an [Iterable] of [Record] instances.
 *
 * This is the natural access mode for formats like CSV, Excel, and Avro,
 * where data is read row-by-row.
 */
interface FlowRecordSource : RecordSource, Iterable<Record>

/**
 * Columnar record source — produces data as [VectorBlockIterator].
 *
 * This is the natural access mode for columnar formats like Parquet,
 * where data is stored and read in column chunks.
 *
 * @see VectorBlockIterator
 */
interface FlowVectorSource : RecordSource {
    /**
     * Returns a [VectorBlockIterator] that yields vector blocks of the
     * specified [batchSize].
     *
     * @param batchSize number of rows per vector block (default 1024)
     */
    fun vectorBlocks(batchSize: Int = 1024): VectorBlockIterator
}
