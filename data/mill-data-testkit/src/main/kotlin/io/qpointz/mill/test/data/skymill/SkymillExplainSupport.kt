package io.qpointz.mill.test.data.skymill

import io.qpointz.mill.data.backend.calcite.CalciteContext
import io.qpointz.mill.test.data.backend.FlowBackendContextRunner
import org.apache.calcite.jdbc.CalciteConnection

fun FlowBackendContextRunner.explainPlan(sql: String): String =
    calciteContextFactory.createContext().use { ctx ->
        SkymillExplainSupport.explainPlanFor(ctx.calciteConnection, sql)
    }

/**
 * Helpers for parsing Skymill physical plans and asserting join shape.
 */
object SkymillExplainSupport {

    data class JoinPlanShape(
        val hashJoinCount: Int,
        val mergeJoinCount: Int,
        val sortCount: Int,
        val tableScanMentions: Map<String, Int>,
    )

    /**
     * JDBC `EXPLAIN PLAN FOR` — runs full Volcano optimization and may execute the query.
     * Prefer Hep-based explain helpers in calcite test fixtures for multi-join plan-shape tests
     * when table statistics are unknown.
     */
    fun explainPlanFor(connection: CalciteConnection, sql: String): String {
        val explainSql = "EXPLAIN PLAN FOR $sql"
        connection.createStatement().use { statement ->
            statement.executeQuery(explainSql).use { rs ->
                return buildString {
                    while (rs.next()) {
                        appendLine(rs.getString(1))
                    }
                }
            }
        }
    }

    fun explainPlanFor(context: CalciteContext, sql: String): String =
        explainPlanFor(context.calciteConnection, sql)

    fun countPhysicalOperator(explain: String, operator: String): Int {
        var count = 0
        var index = 0
        while (true) {
            val found = explain.indexOf(operator, index)
            if (found < 0) {
                return count
            }
            count++
            index = found + operator.length
        }
    }

    fun countTableMentions(explain: String, tableName: String): Int =
        countPhysicalOperator(explain, tableName)

    fun parseJoinPlanShape(
        explain: String,
        tableNames: Iterable<String> = DEFAULT_SCAN_TABLES,
    ): JoinPlanShape = JoinPlanShape(
        hashJoinCount = countPhysicalOperator(explain, "EnumerableHashJoin"),
        mergeJoinCount = countPhysicalOperator(explain, "EnumerableMergeJoin"),
        sortCount = countPhysicalOperator(explain, "EnumerableSort"),
        tableScanMentions = tableNames.associateWith { countTableMentions(explain, it) },
    )

    fun assertHashJoinBiased(
        explain: String,
        maxMergeJoins: Int = 0,
        minHashJoins: Int = 1,
    ) {
        val shape = parseJoinPlanShape(explain)
        require(shape.hashJoinCount >= minHashJoins) {
            "expected at least $minHashJoins EnumerableHashJoin, got ${shape.hashJoinCount}:\n$explain"
        }
        require(shape.mergeJoinCount <= maxMergeJoins) {
            "expected at most $maxMergeJoins EnumerableMergeJoin, got ${shape.mergeJoinCount}:\n$explain"
        }
    }

    fun assertExpectedTableScanMentions(shape: JoinPlanShape) {
        require(shape.tableScanMentions.getValue("cities") >= 3) {
            "expected cities scanned at least 3 times, got ${shape.tableScanMentions}"
        }
        for (table in LARGE_FACT_TABLES) {
            require(shape.tableScanMentions.getValue(table) >= 1) {
                "expected $table in explain plan, got ${shape.tableScanMentions}"
            }
        }
    }

    private val DEFAULT_SCAN_TABLES = listOf(
        "bookings",
        "passenger",
        "flight_instances",
        "segments",
        "cities",
    )

    private val LARGE_FACT_TABLES = listOf(
        "bookings",
        "passenger",
        "flight_instances",
        "segments",
    )
}
