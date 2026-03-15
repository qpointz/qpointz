package io.qpointz.mill.ai.capabilities.sqldialect

import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Handler unit tests for [SqlDialectToolHandlers].
 *
 * Uses a real Calcite [SqlDialectSpec] fixture to give realistic coverage without
 * hand-building the full nested dialect model. Each test targets a single handler function
 * and validates both the happy-path and the deterministic unknown-input cases.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SqlDialectToolHandlersTest {

    private lateinit var spec: SqlDialectSpec

    @BeforeAll
    fun loadCalciteFixture() {
        spec = DialectRegistry.fromClasspathDefaults().requireDialect("calcite")
    }

    // ── getSqlDialectConventions ───────────────────────────────────────────────

    @Test
    fun `getSqlDialectConventions returns dialect id and name`() {
        val result = SqlDialectToolHandlers.getSqlDialectConventions(spec)
        assertEquals("CALCITE", result.dialectId)
        assertEquals("Apache Calcite", result.dialectName)
    }

    @Test
    fun `getSqlDialectConventions returns identifier quoting rules`() {
        val result = SqlDialectToolHandlers.getSqlDialectConventions(spec)
        assertEquals("`", result.identifiers.quoteStart)
        assertEquals("`", result.identifiers.quoteEnd)
        assertTrue(result.identifiers.useFullyQualifiedNames)
    }

    @Test
    fun `getSqlDialectConventions returns literal rules`() {
        val result = SqlDialectToolHandlers.getSqlDialectConventions(spec)
        assertEquals("'", result.literals.stringQuote)
        assertEquals("NULL", result.literals.nullLiteral)
        assertTrue(result.literals.booleanLiterals.contains("TRUE"))
        assertTrue(result.literals.booleanLiterals.contains("FALSE"))
    }

    @Test
    fun `getSqlDialectConventions returns function categories`() {
        val result = SqlDialectToolHandlers.getSqlDialectConventions(spec)
        assertFalse(result.functionCategories.isEmpty())
        assertTrue(result.functionCategories.contains("aggregates"))
        assertTrue(result.functionCategories.contains("strings"))
    }

    @Test
    fun `getSqlDialectConventions function categories are sorted`() {
        val result = SqlDialectToolHandlers.getSqlDialectConventions(spec)
        assertEquals(result.functionCategories.sorted(), result.functionCategories)
    }

    // ── getSqlPagingRules ─────────────────────────────────────────────────────

    @Test
    fun `getSqlPagingRules returns at least one paging style`() {
        val result = SqlDialectToolHandlers.getSqlPagingRules(spec)
        assertFalse(result.styles.isEmpty())
    }

    @Test
    fun `getSqlPagingRules Calcite has standard LIMIT style`() {
        val result = SqlDialectToolHandlers.getSqlPagingRules(spec)
        val standard = result.styles.find { it.type == "standard" }
        assertNotNull(standard)
        assertTrue(standard!!.syntax.contains("LIMIT"))
    }

    @Test
    fun `getSqlPagingRules returns offset value`() {
        val result = SqlDialectToolHandlers.getSqlPagingRules(spec)
        assertFalse(result.offset.isBlank())
    }

    // ── getSqlJoinRules ───────────────────────────────────────────────────────

    @Test
    fun `getSqlJoinRules returns join style`() {
        val result = SqlDialectToolHandlers.getSqlJoinRules(spec)
        assertFalse(result.style.isBlank())
    }

    @Test
    fun `getSqlJoinRules returns five join type entries`() {
        val result = SqlDialectToolHandlers.getSqlJoinRules(spec)
        val names = result.joinTypes.map { it.name }.toSet()
        assertEquals(setOf("CROSS", "INNER", "LEFT", "RIGHT", "FULL"), names)
    }

    @Test
    fun `getSqlJoinRules returns ON clause keyword`() {
        val result = SqlDialectToolHandlers.getSqlJoinRules(spec)
        assertFalse(result.onClauseKeyword.isBlank())
    }

    // ── getSqlFunctions ───────────────────────────────────────────────────────

    @Test
    fun `getSqlFunctions returns functions for known category`() {
        val result = SqlDialectToolHandlers.getSqlFunctions(spec, "aggregates")
        assertInstanceOf(SqlDialectToolHandlers.SqlFunctionsResult::class.java, result)
        result as SqlDialectToolHandlers.SqlFunctionsResult
        assertEquals("aggregates", result.category)
        assertFalse(result.functions.isEmpty())
    }

    @Test
    fun `getSqlFunctions result contains COUNT for aggregates`() {
        val result = SqlDialectToolHandlers.getSqlFunctions(spec, "aggregates")
                as SqlDialectToolHandlers.SqlFunctionsResult
        assertTrue(result.functions.any { it.name == "COUNT" })
    }

    @Test
    fun `getSqlFunctions returns error for unknown category`() {
        val result = SqlDialectToolHandlers.getSqlFunctions(spec, "does_not_exist")
        assertInstanceOf(SqlDialectToolHandlers.UnknownCategoryResult::class.java, result)
        result as SqlDialectToolHandlers.UnknownCategoryResult
        assertTrue(result.error.contains("does_not_exist"))
        assertFalse(result.availableCategories.isEmpty())
    }

    @Test
    fun `getSqlFunctions unknown category result lists available categories`() {
        val result = SqlDialectToolHandlers.getSqlFunctions(spec, "no_such")
                as SqlDialectToolHandlers.UnknownCategoryResult
        assertTrue(result.availableCategories.contains("aggregates"))
    }

    // ── getSqlFunctionInfo ────────────────────────────────────────────────────

    @Test
    fun `getSqlFunctionInfo returns full info for known function`() {
        val result = SqlDialectToolHandlers.getSqlFunctionInfo(spec, "COUNT")
        assertInstanceOf(SqlDialectToolHandlers.FunctionInfo::class.java, result)
        result as SqlDialectToolHandlers.FunctionInfo
        assertEquals("COUNT", result.name)
        assertEquals("aggregates", result.category)
        assertFalse(result.syntax.isBlank())
        assertFalse(result.returnType.isBlank())
    }

    @Test
    fun `getSqlFunctionInfo maps return type correctly`() {
        val result = SqlDialectToolHandlers.getSqlFunctionInfo(spec, "COUNT")
                as SqlDialectToolHandlers.FunctionInfo
        assertEquals("INTEGER", result.returnType)
        assertFalse(result.returnNullable)
    }

    @Test
    fun `getSqlFunctionInfo maps args list`() {
        val result = SqlDialectToolHandlers.getSqlFunctionInfo(spec, "COUNT")
                as SqlDialectToolHandlers.FunctionInfo
        assertFalse(result.args.isEmpty())
        val arg = result.args.first()
        assertFalse(arg.name.isBlank())
        assertFalse(arg.type.isBlank())
    }

    @Test
    fun `getSqlFunctionInfo returns error for unknown function`() {
        val result = SqlDialectToolHandlers.getSqlFunctionInfo(spec, "NO_SUCH_FUNCTION_XYZ")
        assertInstanceOf(SqlDialectToolHandlers.UnknownFunctionResult::class.java, result)
        result as SqlDialectToolHandlers.UnknownFunctionResult
        assertTrue(result.error.contains("NO_SUCH_FUNCTION_XYZ"))
    }
}
