package io.qpointz.mill.source

import io.qpointz.mill.sql.RecordReaders
import io.qpointz.mill.vectors.VectorBlockIterator

/**
 * A logical table backed by one or more files.
 *
 * A [SourceTable] unions multiple [RecordSource] instances (one per file)
 * into a single table with both row-oriented and columnar access paths.
 * It maps to a mill `Table` within a schema.
 *
 * @see MultiFileSourceTable
 */
interface SourceTable {

    /** Schema describing the columns of this table. */
    val schema: RecordSchema

    /**
     * Row-oriented access: returns all records from all underlying files
     * as a concatenated [Iterable].
     */
    fun records(): Iterable<Record>

    /**
     * Columnar access: returns all data from all underlying files
     * as a concatenated [VectorBlockIterator].
     *
     * @param batchSize number of rows per vector block (default 1024)
     */
    fun vectorBlocks(batchSize: Int = 1024): VectorBlockIterator

    /**
     * Returns a mill-core [io.qpointz.mill.sql.RecordReader] wrapping
     * the columnar path for integration with the mill query engine.
     *
     * @param batchSize number of rows per vector block (default 1024)
     */
    fun asMillRecordReader(batchSize: Int = 1024): io.qpointz.mill.sql.RecordReader {
        return RecordReaders.recordReader(vectorBlocks(batchSize))
    }
}
