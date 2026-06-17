package io.qpointz.mill.source.calcite

import io.qpointz.mill.test.data.skymill.SkymillDataset
import io.qpointz.mill.test.data.skymill.SkymillExplainSupport
import io.qpointz.mill.test.data.skymill.SkymillTestFixtures
import org.apache.calcite.adapter.enumerable.EnumerableRules
import org.apache.calcite.jdbc.CalciteConnection
import org.apache.calcite.plan.hep.HepMatchOrder
import org.apache.calcite.plan.hep.HepPlanner
import org.apache.calcite.plan.hep.HepProgram
import org.apache.calcite.sql.parser.SqlParser
import org.apache.calcite.tools.Frameworks
import java.sql.DriverManager
import java.util.Properties

/**
 * Calcite session fixtures for Skymill planner tests.
 *
 * Analogous to [FlowCalciteTestFixtures] but materialises the real Skymill schema via
 * [SourceSchemaManager]. Dataset descriptors live in [SkymillTestFixtures]; join SQL in
 * [SkymillJoinQueries] (this module's unit test sources).
 */
object SkymillCalciteTestFixtures {

    fun openSession(dataset: SkymillDataset = SkymillDataset.CSV): SkymillCalciteSession {
        val schemaManager = SourceSchemaManager()
        schemaManager.add(SkymillTestFixtures.descriptorFor(dataset))
        Class.forName("org.apache.calcite.jdbc.Driver")
        val connection = DriverManager
            .getConnection("jdbc:calcite:", calciteConnectionProperties())
            .unwrap(CalciteConnection::class.java)
        schemaManager.registerAll(connection.rootSchema)
        connection.schema = SkymillTestFixtures.SCHEMA_NAME
        return SkymillCalciteSession(connection, schemaManager)
    }

    fun frameworkConfig(session: SkymillCalciteSession) =
        Frameworks.newConfigBuilder()
            .defaultSchema(session.connection.rootSchema.getSubSchema(SkymillTestFixtures.SCHEMA_NAME))
            .parserConfig(parserConfig(session.connection))
            .build()

    /**
     * Returns a physical enumerable plan as text without executing the query.
     *
     * JDBC `EXPLAIN PLAN FOR` re-runs Volcano cost-based optimization and can explore
     * hundreds of thousands of rule matches on multi-join Skymill queries when table
     * statistics are unknown — use this method for join-policy assertions instead.
     */
    fun explainPhysicalPlan(session: SkymillCalciteSession, sql: String): String {
        val frameworkConfig = frameworkConfig(session)
        val planner = Frameworks.getPlanner(frameworkConfig)
        val logical = planner.rel(planner.validate(planner.parse(sql))).rel

        val hepProgram = HepProgram.builder()
            .addMatchOrder(HepMatchOrder.BOTTOM_UP)
            .addRuleInstance(FlowTableScanToEnumerableRule.INSTANCE)
            .addRuleCollection(EnumerableRules.rules())
            .build()
        val hepPlanner = HepPlanner(hepProgram, frameworkConfig.context)
        hepPlanner.root = logical
        return hepPlanner.findBestExp().explain()
    }

    /**
     * Production-style explain via JDBC. Avoid on Skymill multi-join SQL: triggers full
     * Volcano optimization and may run the query (very slow without table statistics / join policy).
     */
    fun explainJdbcPlan(session: SkymillCalciteSession, sql: String): String =
        SkymillExplainSupport.explainPlanFor(session.connection, sql)

    private fun parserConfig(connection: CalciteConnection): SqlParser.Config {
        val connectionConfig = connection.config()
        return SqlParser.Config.DEFAULT
            .withConformance(connectionConfig.conformance())
            .withCaseSensitive(connectionConfig.caseSensitive())
            .withLex(connectionConfig.lex())
            .withQuoting(connectionConfig.quoting())
    }

    private fun calciteConnectionProperties(): Properties =
        Properties().apply {
            setProperty("quoting", "BACK_TICK")
            setProperty("caseSensitive", "false")
            setProperty("unquotedCasing", "TO_UPPER")
        }
}

class SkymillCalciteSession(
    val connection: CalciteConnection,
    private val schemaManager: SourceSchemaManager,
) : AutoCloseable {

    /** Physical enumerable plan text; does not execute the query. */
    fun explainPhysicalPlan(sql: String): String =
        SkymillCalciteTestFixtures.explainPhysicalPlan(this, sql)

    /** JDBC `EXPLAIN PLAN FOR`; slow on Skymill multi-join without stats. */
    fun explainJdbcPlan(sql: String): String =
        SkymillCalciteTestFixtures.explainJdbcPlan(this, sql)

    override fun close() {
        schemaManager.close()
        connection.close()
    }
}
