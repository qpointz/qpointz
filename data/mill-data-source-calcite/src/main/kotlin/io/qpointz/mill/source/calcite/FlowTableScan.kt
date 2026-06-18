package io.qpointz.mill.source.calcite

import org.apache.calcite.plan.Convention
import org.apache.calcite.plan.RelOptCluster
import org.apache.calcite.plan.RelOptPlanner
import org.apache.calcite.plan.RelOptTable
import org.apache.calcite.plan.RelTraitSet
import org.apache.calcite.rel.RelNode
import org.apache.calcite.rel.core.TableScan
import org.apache.calcite.rel.hint.RelHint
import org.apache.calcite.rel.metadata.RelMetadataQuery

/**
 * Logical table-scan [RelNode] for Mill Flow tables.
 *
 * Produced by [FlowTable.toRel] during SQL / Substrait planning. Starts with
 * [Convention.NONE]; enumerable conversion rules later call [copy] with
 * [org.apache.calcite.adapter.enumerable.EnumerableConvention].
 *
 * Optional pushed filter and project field indices may be added on this node later.
 *
 * @param cluster planner cluster shared by the surrounding [RelNode] tree
 * @param traitSet traits for this scan (logical: [Convention.NONE])
 * @param hints table hints from the active [RelOptTable.ToRelContext]
 * @param table Calcite table handle wrapping the [FlowTable] being scanned
 */
class FlowTableScan(
    cluster: RelOptCluster,
    traitSet: RelTraitSet,
    hints: List<RelHint>,
    table: RelOptTable,
) : TableScan(cluster, traitSet, hints, table) {

    /**
     * Clones this scan when the planner changes traits (e.g. NONE → ENUMERABLE).
     *
     * Table scans have no inputs; only [traitSet] may change. Pushdown rules
     * will extend this to copy additional pushed state on new instances.
     *
     * @param traitSet new trait set assigned by the planner or a converter rule
     * @param inputs child nodes (must be empty for a scan)
     */
    override fun copy(traitSet: RelTraitSet, inputs: List<RelNode>): RelNode {
        require(inputs.isEmpty()) { "FlowTableScan must have no inputs" }
        return FlowTableScan(cluster, traitSet, hints, table)
    }

    /**
     * Row estimate for join costing and hash build-side selection (WI-315).
     *
     * Enumerable join rules query [RelMetadataQuery.getRowCount] on the scan [RelNode], not on
     * [FlowTable] directly. [TableScan.estimateRowCount] defaults to [RelOptTable.getRowCount];
     * this override reads Mill [io.qpointz.mill.source.SourceTable] statistics explicitly so
     * planner metadata matches wired providers even before pushdown adjusts selectivity.
     */
    override fun estimateRowCount(mq: RelMetadataQuery): Double {
        val flowTable = table.unwrap(FlowTable::class.java) ?: return super.estimateRowCount(mq)
        val rowCount = FlowTableStatistics.toCalciteStatistic(
            flowTable.sourceTable().statisticProviders(),
        ).rowCount
        return rowCount ?: super.estimateRowCount(mq)
    }

    companion object {
        /**
         * Factory used from [FlowTable.toRel].
         *
         * @param context planner to-rel context supplying cluster and table hints
         * @param relOptTable table being scanned
         */
        @JvmStatic
        fun create(context: RelOptTable.ToRelContext, relOptTable: RelOptTable): FlowTableScan {
            val cluster = context.cluster
            return FlowTableScan(
                cluster,
                cluster.traitSetOf(Convention.NONE),
                context.tableHints,
                relOptTable,
            )
        }
    }

    /**
     * Registers planner rules the first time this [RelNode] class is seen.
     *
     * [FlowTableScanToEnumerableRule] bridges logical scans to
     * [org.apache.calcite.adapter.enumerable.EnumerableTableScan]. [FlowEnumerableRuleSets]
     * supplies the remaining enumerable physical rules (hash join, filter, project, etc.) with
     * merge join excluded for unsorted file scans.
     *
     * Volcano calls this hook via [org.apache.calcite.plan.RelOptPlanner.registerClass] during
     * `RelRunner.prepareStatement` / JDBC execution. Hep explain helpers use
     * [FlowRelPlannerRules.registerRulesFromRelTree] to apply the same rules.
     */
    override fun register(planner: RelOptPlanner) {
        planner.addRule(FlowTableScanToEnumerableRule.INSTANCE)
        FlowEnumerableRuleSets.register(planner)
    }
}
