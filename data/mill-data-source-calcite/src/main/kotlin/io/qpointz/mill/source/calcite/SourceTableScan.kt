package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.SourceTable
import org.apache.calcite.linq4j.AbstractEnumerable
import org.apache.calcite.linq4j.Enumerable
import org.apache.calcite.linq4j.Enumerator

/**
 * Mill-native row iterator that materializes [io.qpointz.mill.source.Record]s as Calcite `Object[]` rows.
 *
 * Shared by [FlowTable.scan]. Each output row follows
 * [io.qpointz.mill.source.RecordSchema] field order. Optional column projection
 * may be added before record materialization.
 *
 * @property sourceTable Mill table whose [SourceTable.records] are iterated
 */
class SourceTableScan(
    private val sourceTable: SourceTable,
) {

    /**
     * Returns an [Enumerable] of full rows in schema column order.
     *
     * Closes the underlying record iterator when enumeration completes or when
     * the [Enumerator] is closed and the iterator implements [AutoCloseable].
     */
    fun scan(): Enumerable<Array<Any?>> {
        val schema = sourceTable.schema
        val fieldNames = schema.fieldNames
        return object : AbstractEnumerable<Array<Any?>>() {
            override fun enumerator(): Enumerator<Array<Any?>> {
                val records = sourceTable.records().iterator()

                return object : Enumerator<Array<Any?>> {
                    private var current: Array<Any?> = emptyArray()
                    private var closed = false

                    override fun current(): Array<Any?> = current

                    override fun moveNext(): Boolean {
                        if (closed) return false
                        if (!records.hasNext()) {
                            close()
                            return false
                        }
                        val record = records.next()
                        current = Array(fieldNames.size) { i -> record[fieldNames[i]] }
                        return true
                    }

                    override fun reset() {
                        throw UnsupportedOperationException("reset() is not supported")
                    }

                    override fun close() {
                        if (closed) return
                        closed = true
                        if (records is AutoCloseable) {
                            records.close()
                        }
                    }
                }
            }
        }
    }
}
