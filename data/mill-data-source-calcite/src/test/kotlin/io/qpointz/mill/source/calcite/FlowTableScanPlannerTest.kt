package io.qpointz.mill.source.calcite

import io.qpointz.mill.test.data.calcite.RelOptTestSupport
import org.apache.calcite.jdbc.CalciteConnection
import org.apache.calcite.plan.Convention
import org.apache.calcite.rel.RelNode
import org.apache.calcite.rel.core.Filter
import org.apache.calcite.rel.logical.LogicalTableScan
import org.apache.calcite.schema.SchemaPlus
import org.apache.calcite.tools.Frameworks
import org.apache.calcite.tools.RelBuilder
import org.apache.calcite.tools.RelRunner
import org.apache.calcite.plan.hep.HepPlanner
import org.apache.calcite.plan.hep.HepProgram
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Planner contract tests for [FlowTableScan].
 *
 * Covers logical [FlowTableScan] (`Convention.NONE`), enumerable conversion to
 * [EnumerableTableScan], and the current filter placement above the scan (before
 * filter/project pushdown lands).
 *
 * Use quoted identifiers in SQL (`"users"`, `"id"`) — Calcite folds unquoted names to uppercase.
 */
class FlowTableScanPlannerTest {

  private lateinit var connection: CalciteConnection
  private lateinit var rootSchema: SchemaPlus

  @BeforeEach
  fun setUp() {
    val opened = FlowCalciteTestFixtures.openCalciteConnection()
    connection = opened.first
    rootSchema = opened.second
  }

  @AfterEach
  fun tearDown() {
    connection.close()
  }

  @Test
  fun shouldEmitFlowTableScan_whenBuiltWithRelBuilder() {
    val rel = RelBuilder.create(frameworkConfig())
      .scan("users")
      .build()
    RelOptTestSupport.logPlan("shouldEmitFlowTableScan_whenBuiltWithRelBuilder", rel)

    val scans = rel.findNodes(FlowTableScan::class.java)
    assertEquals(1, scans.size, rel.planDigest())
    assertEquals(Convention.NONE, scans.single().traitSet.convention, rel.planDigest())
    assertFalse(
      rel.findNodes(LogicalTableScan::class.java).isNotEmpty(),
      "TranslatableTable must not emit LogicalTableScan: ${rel.planDigest()}",
    )
  }

  @Test
  fun shouldEmitFlowTableScan_whenSqlPlanned() {
    val rel = planSql(SELECT_ID_FROM_USERS)
    RelOptTestSupport.logPlan("shouldEmitFlowTableScan_whenSqlPlanned", rel)

    val scans = rel.findNodes(FlowTableScan::class.java)
    assertEquals(1, scans.size, rel.planDigest())
    assertEquals(Convention.NONE, scans.single().traitSet.convention, rel.planDigest())
  }

  /**
   * Baseline before filter pushdown: filter stays above [FlowTableScan].
   * After pushdown, change this to assert filter is merged into the scan node.
   */
  @Test
  fun shouldKeepFilterAboveFlowTableScan_beforePushdown() {
    val rel = planSql(SELECT_NAME_WHERE_ID)
    RelOptTestSupport.logPlan("shouldKeepFilterAboveFlowTableScan_beforePushdown", rel)

    assertEquals(1, rel.findNodes(Filter::class.java).size, rel.planDigest())
  }

  /**
   * Execution path: [FlowTableScan] must convert to [EnumerableTableScan].
   * Requires [FlowTableScan.register] to add the enumerable converter rule.
   */
  @Test
  fun shouldRegisterFlowEnumerableRules_whenFlowTableScanRegistersClass() {
    val logical = planSql(SELECT_ID_FROM_USERS)
    val planner = HepPlanner(HepProgram.builder().build())
    FlowRelPlannerRules.registerRulesFromRelTree(logical, planner)

    val descriptions = planner.rules.map { it.toString() }
    assertTrue(descriptions.any { it.contains("FlowTableScanToEnumerableRule") })
    assertFalse(descriptions.any { it.contains("EnumerableMergeJoinRule") })
  }

  @Test
  fun shouldConvertToEnumerableTableScan_whenPreparedForExecution() {
    val logical = planSql(SELECT_ID_FROM_USERS)
    RelOptTestSupport.logPlan("shouldConvertToEnumerableTableScan_whenPreparedForExecution (logical)", logical)
    connection.unwrap(RelRunner::class.java).prepareStatement(logical).close()

    val explain = explainPlanFor(SELECT_ID_FROM_USERS)
    RelOptTestSupport.logExplain("shouldConvertToEnumerableTableScan_whenPreparedForExecution", explain)
    assertTrue(
      explain.contains("EnumerableTableScan"),
      "expected physical EnumerableTableScan in explain plan:\n$explain",
    )
    assertFalse(
      explain.contains("LogicalTableScan"),
      "generic logical scan should not appear after conversion:\n$explain",
    )
  }

  private fun frameworkConfig() =
    FlowCalciteTestFixtures.frameworkConfig(rootSchema)

  private fun planSql(sql: String): RelNode {
    val planner = Frameworks.getPlanner(frameworkConfig())
    val parsed = planner.parse(sql)
    val validated = planner.validate(parsed)
    return planner.rel(validated).rel
  }

  private fun explainPlanFor(sql: String): String {
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

  private fun RelNode.planDigest(): String = RelOptTestSupport.planDigest(this)

  private fun <T : RelNode> RelNode.findNodes(type: Class<T>): List<T> =
    RelOptTestSupport.findNodes(this, type)

  companion object {
    private const val SELECT_ID_FROM_USERS = """SELECT "id" FROM "users""""
    private const val SELECT_NAME_WHERE_ID =
      """SELECT "name" FROM "users" WHERE "id" = 2"""
  }
}
