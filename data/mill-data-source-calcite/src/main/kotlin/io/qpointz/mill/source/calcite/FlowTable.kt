package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.SourceTable
import org.apache.calcite.DataContext
import org.apache.calcite.linq4j.Enumerable
import org.apache.calcite.plan.RelOptTable
import org.apache.calcite.rel.RelNode
import org.apache.calcite.rel.type.RelDataType
import org.apache.calcite.rel.type.RelDataTypeFactory
import org.apache.calcite.schema.ScannableTable
import org.apache.calcite.schema.TranslatableTable
import org.apache.calcite.schema.impl.AbstractTable

/**
 * Calcite schema [org.apache.calcite.schema.Table] backed by a Mill [SourceTable].
 *
 * Each logical Mill table registered in [FlowSchema] is a [FlowTable] instance.
 * Calcite uses two hooks on the same object:
 *
 * - [TranslatableTable.toRel] — planning: emit a custom [FlowTableScan] instead of a
 *   generic [org.apache.calcite.rel.logical.LogicalTableScan].
 * - [ScannableTable.scan] — execution: enumerable rules delegate row iteration here
 *   after [FlowTableScan] is converted to [org.apache.calcite.adapter.enumerable.EnumerableTableScan].
 *
 * Filter and project pushdown are **not** on this class; they attach to [FlowTableScan]
 * via planner rules.
 *
 * @property sourceTable underlying Mill source (possibly multi-file)
 */
class FlowTable(
    private val sourceTable: SourceTable,
) : AbstractTable(), TranslatableTable, ScannableTable {

    /**
     * Returns the Mill [SourceTable] behind this Calcite table.
     *
     * Used by tests, enumerable bindables, and future table statistics.
     */
    fun sourceTable(): SourceTable = sourceTable

    /**
     * Derives Calcite column types from the Mill [io.qpointz.mill.source.RecordSchema].
     *
     * @param typeFactory Calcite type factory from the active planner or connection
     */
    override fun getRowType(typeFactory: RelDataTypeFactory): RelDataType =
        CalciteTypeMapper.toRelDataType(sourceTable.schema, typeFactory)

    /**
     * Creates the logical scan [RelNode] for this table.
     *
     * @param context planner context (cluster, table hints)
     * @param relOptTable Calcite table handle for this [FlowTable]
     * @return a [FlowTableScan] with [org.apache.calcite.plan.Convention.NONE]
     */
    override fun toRel(
        context: RelOptTable.ToRelContext,
        relOptTable: RelOptTable,
    ): RelNode = FlowTableScan.create(context, relOptTable)

    /**
     * Row-oriented full-table scan used by the enumerable executor.
     *
     * Reads all records via [SourceTableScan]; selective projection and predicates
     * attach to [FlowTableScan] via planner rules before or during this path.
     *
     * @param root Calcite execution context (unused for Mill-native iteration today)
     */
    override fun scan(root: DataContext): Enumerable<Array<Any?>> =
        SourceTableScan(sourceTable).scan()
}
