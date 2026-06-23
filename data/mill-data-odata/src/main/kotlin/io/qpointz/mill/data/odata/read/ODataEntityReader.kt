package io.qpointz.mill.data.odata.read

import io.qpointz.mill.sql.RecordReader
import io.qpointz.mill.sql.RecordReaders
import io.qpointz.mill.vectors.VectorBlockIterator

/**
 * Converts {@link VectorBlockIterator} rows to maps for OData JSON rendering.
 */
class ODataEntityReader {

    /**
     * @param iterator dispatcher result iterator
     * @param maxRows optional row cap
     * @return list of column-name → value maps
     */
    fun readAll(iterator: VectorBlockIterator, maxRows: Int = Int.MAX_VALUE): List<Map<String, Any?>> {
        val reader: RecordReader = RecordReaders.recordReader(iterator)
        val rows = mutableListOf<Map<String, Any?>>()
        try {
            while (reader.hasNext() && rows.size < maxRows) {
                reader.next()
                val row = linkedMapOf<String, Any?>()
                for (col in 0 until reader.columnCount) {
                    val name = reader.getColumnMetadata(col).name
                    row[name] = if (reader.isNull(col)) null else reader.getObject(col)
                }
                rows += row
            }
        } finally {
            reader.close()
        }
        return rows
    }
}
