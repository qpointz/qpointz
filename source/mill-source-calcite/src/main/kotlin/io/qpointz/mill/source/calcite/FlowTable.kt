package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.SourceTable
import org.apache.calcite.DataContext
import org.apache.calcite.linq4j.AbstractEnumerable
import org.apache.calcite.linq4j.Enumerable
import org.apache.calcite.linq4j.Enumerator
import org.apache.calcite.rel.type.RelDataType
import org.apache.calcite.rel.type.RelDataTypeFactory
import org.apache.calcite.schema.ScannableTable
import org.apache.calcite.schema.impl.AbstractTable

/**
 * Calcite [ScannableTable] backed by a Mill [SourceTable].
 *
 * Row type is derived from the source table's [io.qpointz.mill.source.RecordSchema]
 * via [CalciteTypeMapper]. Scanning iterates over all [io.qpointz.mill.source.Record]s
 * from the underlying (potentially multi-file) source table and projects each record
 * into an `Object[]` in schema-field order.
 *
 * @property sourceTable the Mill source table to expose to Calcite
 */
class FlowTable(
    private val sourceTable: SourceTable
) : AbstractTable(), ScannableTable {

    override fun getRowType(typeFactory: RelDataTypeFactory): RelDataType =
        CalciteTypeMapper.toRelDataType(sourceTable.schema, typeFactory)

    override fun scan(root: DataContext): Enumerable<Array<Any?>> {
        val schema = sourceTable.schema
        val fieldNames = schema.fieldNames

        return object : AbstractEnumerable<Array<Any?>>() {
            override fun enumerator(): Enumerator<Array<Any?>> {
                val records = sourceTable.records().iterator()

                return object : Enumerator<Array<Any?>> {
                    private var current: Array<Any?> = emptyArray()

                    override fun current(): Array<Any?> = current

                    override fun moveNext(): Boolean {
                        if (!records.hasNext()) return false
                        val record = records.next()
                        current = Array(fieldNames.size) { i -> record[fieldNames[i]] }
                        return true
                    }

                    override fun reset() {
                        throw UnsupportedOperationException("reset() is not supported")
                    }

                    override fun close() {
                        // no-op — record iterator is not closeable
                    }
                }
            }
        }
    }
}
