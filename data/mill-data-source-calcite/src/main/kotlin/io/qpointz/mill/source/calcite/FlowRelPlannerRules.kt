package io.qpointz.mill.source.calcite

import org.apache.calcite.plan.Context
import org.apache.calcite.plan.RelOptPlanner
import org.apache.calcite.plan.hep.HepMatchOrder
import org.apache.calcite.plan.hep.HepPlanner
import org.apache.calcite.plan.hep.HepProgram
import org.apache.calcite.rel.RelHomogeneousShuttle
import org.apache.calcite.rel.RelNode

/**
 * Helpers for applying rules registered on Mill [RelNode] types (not JDBC connection config).
 *
 * Mirrors Volcano behaviour: the first time a [RelNode] class appears, [RelOptPlanner.registerClass]
 * invokes [RelNode.register], which for [FlowTableScan] wires [FlowEnumerableRuleSets].
 */
object FlowRelPlannerRules {

    /**
     * Invokes [RelOptPlanner.registerClass] once per distinct [RelNode] implementation in [root].
     *
     * @param root logical or physical plan root
     * @param planner planner that receives rules from node registration hooks
     */
    @JvmStatic
    fun registerRulesFromRelTree(root: RelNode, planner: RelOptPlanner) {
        val seen = mutableSetOf<Class<*>>()
        root.accept(object : RelHomogeneousShuttle() {
            override fun visit(rel: RelNode): RelNode {
                if (seen.add(rel.javaClass)) {
                    planner.registerClass(rel)
                }
                return super.visit(rel)
            }
        })
    }

    /**
     * Produces an enumerable physical plan digest using rules discovered from [logical].
     *
     * @param logical validated logical relational plan (typically containing [FlowTableScan])
     * @param context Calcite planner context (from [org.apache.calcite.tools.FrameworkConfig])
     */
    @JvmStatic
    fun explainEnumerablePhysicalPlan(logical: RelNode, context: Context): String {
        val bootstrap = HepPlanner(HepProgram.builder().build(), context)
        registerRulesFromRelTree(logical, bootstrap)
        val program = HepProgram.builder()
            .addMatchOrder(HepMatchOrder.BOTTOM_UP)
            .addRuleCollection(bootstrap.rules)
            .build()
        val hepPlanner = HepPlanner(program, context)
        hepPlanner.root = logical
        return hepPlanner.findBestExp().explain()
    }
}
