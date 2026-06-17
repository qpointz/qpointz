package io.qpointz.mill.test.data.calcite

import org.apache.calcite.rel.RelHomogeneousShuttle
import org.apache.calcite.rel.RelNode
import org.slf4j.LoggerFactory

/**
 * Shared helpers for walking and logging [RelNode] trees in Calcite planner tests.
 */
object RelOptTestSupport {

    private val log = LoggerFactory.getLogger(RelOptTestSupport::class.java)

    /**
     * Logs a labelled logical or physical plan digest at INFO when the logger is enabled.
     *
     * @param label test or assertion context shown in log output
     * @param root relational plan root
     */
    fun logPlan(label: String, root: RelNode) {
        if (!log.isInfoEnabled) {
            return
        }
        log.info("=== {} ===\n{}", label, planDigest(root))
    }

    /**
     * Logs JDBC `EXPLAIN PLAN FOR` text at INFO when the logger is enabled.
     *
     * @param label test or assertion context shown in log output
     * @param explain explain plan text
     */
    fun logExplain(label: String, explain: String) {
        if (!log.isInfoEnabled) {
            return
        }
        log.info("=== {} (EXPLAIN PLAN FOR) ===\n{}", label, explain.trimEnd())
    }

    /**
     * Returns Calcite's standard plan digest from [RelNode.explain].
     */
    fun planDigest(root: RelNode): String = root.explain()

    fun <T : RelNode> findNodes(root: RelNode, type: Class<T>): List<T> {
        val found = mutableListOf<T>()
        root.accept(object : RelHomogeneousShuttle() {
            override fun visit(rel: RelNode): RelNode {
                if (type.isInstance(rel)) {
                    @Suppress("UNCHECKED_CAST")
                    found.add(rel as T)
                }
                return super.visit(rel)
            }
        })
        return found
    }
}
