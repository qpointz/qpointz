package io.qpointz.mill.source.calcite

import org.apache.calcite.adapter.enumerable.EnumerableRules
import org.apache.calcite.plan.RelOptPlanner
import org.apache.calcite.plan.RelOptRule
import java.util.regex.Pattern

/**
 * Curated enumerable physical rules for Mill [FlowTableScan] plans.
 *
 * Registered from [FlowTableScan.register] so Volcano ([org.apache.calcite.tools.RelRunner],
 * JDBC) and Hep-based explain helpers see the same rule set. JDBC backend connections that never
 * materialise [FlowTableScan] are unaffected.
 */
object FlowEnumerableRuleSets {

    private const val MERGE_JOIN_RULE_DESCRIPTION = "EnumerableMergeJoinRule"

    private val MERGE_JOIN_EXCLUSION: Pattern =
        Pattern.compile(".*EnumerableMergeJoinRule.*")

    /**
     * Standard Calcite enumerable rules for Flow execution, excluding merge join.
     *
     * Merge join on unsorted file scans regresses to sort-heavy plans (see Skymill six-join SQL).
     * Hash join via [org.apache.calcite.adapter.enumerable.EnumerableJoinRule] remains.
     */
    @JvmStatic
    fun rules(): List<RelOptRule> =
        EnumerableRules.rules().filter { rule ->
            !isMergeJoinRule(rule)
        }

    /**
     * Wires Flow enumerable rules on [planner] and suppresses merge join.
     *
     * JDBC `VolcanoPlanner` instances may already include the full [EnumerableRules] bundle
     * before [FlowTableScan] is seen; merge-join rules are removed and excluded here.
     *
     * @param planner active relational optimizer
     */
    @JvmStatic
    fun register(planner: RelOptPlanner) {
        EnumerableRules.rules().forEach { rule ->
            if (isMergeJoinRule(rule)) {
                planner.removeRule(rule)
            }
        }
        rules().forEach { rule -> planner.addRule(rule) }
        planner.setRuleDescExclusionFilter(MERGE_JOIN_EXCLUSION)
    }

    private fun isMergeJoinRule(rule: RelOptRule): Boolean =
        rule.toString().contains(MERGE_JOIN_RULE_DESCRIPTION)
}
