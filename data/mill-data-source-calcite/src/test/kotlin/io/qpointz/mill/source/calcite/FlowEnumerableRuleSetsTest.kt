package io.qpointz.mill.source.calcite

import org.apache.calcite.adapter.enumerable.EnumerableRules
import org.apache.calcite.plan.hep.HepPlanner
import org.apache.calcite.plan.hep.HepProgram
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FlowEnumerableRuleSetsTest {

    @Test
    fun shouldExcludeMergeJoinRule_fromCuratedRules() {
        val fullBundle = EnumerableRules.rules().map { it.toString() }
        val curated = FlowEnumerableRuleSets.rules().map { it.toString() }

        assertTrue(fullBundle.any { it.contains("EnumerableMergeJoinRule") })
        assertFalse(curated.any { it.contains("EnumerableMergeJoinRule") })
        assertTrue(curated.any { it.contains("EnumerableJoinRule") })
        assertTrue(curated.size < fullBundle.size)
    }

    @Test
    fun shouldRegisterCuratedRules_onPlanner() {
        val planner = HepPlanner(HepProgram.builder().build())
        FlowEnumerableRuleSets.register(planner)

        val descriptions = planner.rules.map { it.toString() }
        assertFalse(descriptions.any { it.contains("EnumerableMergeJoinRule") })
        assertTrue(descriptions.any { it.contains("EnumerableJoinRule") })
    }
}
