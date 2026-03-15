package io.qpointz.mill.ai.capabilities.sqldialect

import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.CapabilityDependencies
import io.qpointz.mill.ai.ToolDefinition
import io.qpointz.mill.ai.ToolInvocationSimulator
import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Tool-contract tests for [SqlDialectCapability].
 *
 * Drives each tool through the full JSON-in / JSON-out invocation path via
 * [ToolInvocationSimulator], using a real Calcite [SqlDialectSpec] fixture.
 *
 * Tests verify:
 * - dependency extraction via [SqlDialectCapabilityDependency]
 * - all five expected tools are present
 * - tool descriptions are non-blank
 * - JSON argument strings are correctly deserialized
 * - results contain expected fields and serialize to valid JSON
 * - unknown-input cases return deterministic error objects
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SqlDialectCapabilityToolsTest {

    private lateinit var spec: SqlDialectSpec
    private lateinit var capability: Capability
    private val sim = ToolInvocationSimulator

    @BeforeAll
    fun setup() {
        spec = DialectRegistry.fromClasspathDefaults().requireDialect("calcite")
        capability = SqlDialectCapabilityProvider().create(
            AgentContext(contextType = "general"),
            CapabilityDependencies.of(SqlDialectCapabilityDependency(spec))
        )
    }

    // ── tool wiring ───────────────────────────────────────────────────────────

    @Test
    fun `capability exposes exactly five sql-dialect tools`() {
        assertEquals(
            setOf(
                "get_sql_dialect_conventions",
                "get_sql_paging_rules",
                "get_sql_join_rules",
                "get_sql_functions",
                "get_sql_function_info",
            ),
            capability.tools.map { it.name }.toSet()
        )
    }

    @Test
    fun `all tools have non-blank descriptions`() {
        assertTrue(capability.tools.all { it.description.isNotBlank() })
    }

    @Test
    fun `capability has at least one prompt`() {
        assertFalse(capability.prompts.isEmpty())
    }

    // ── get_sql_dialect_conventions ───────────────────────────────────────────

    @Test
    fun `get_sql_dialect_conventions accepts empty arguments`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_dialect_conventions"), ""))
        assertTrue(result.containsKey("dialectId"))
    }

    @Test
    fun `get_sql_dialect_conventions result contains expected top-level fields`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_dialect_conventions")))
        assertTrue(result.containsKey("dialectId"))
        assertTrue(result.containsKey("dialectName"))
        assertTrue(result.containsKey("identifiers"))
        assertTrue(result.containsKey("literals"))
        assertTrue(result.containsKey("functionCategories"))
    }

    @Test
    fun `get_sql_dialect_conventions identifiers contains quoteStart`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_dialect_conventions")))
        @Suppress("UNCHECKED_CAST")
        val identifiers = result["identifiers"] as Map<String, Any?>
        assertTrue(identifiers.containsKey("quoteStart"))
        assertEquals("`", identifiers["quoteStart"])
    }

    @Test
    fun `get_sql_dialect_conventions functionCategories is a non-empty list`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_dialect_conventions")))
        @Suppress("UNCHECKED_CAST")
        val categories = result["functionCategories"] as List<String>
        assertFalse(categories.isEmpty())
        assertTrue(categories.contains("aggregates"))
    }

    // ── get_sql_paging_rules ──────────────────────────────────────────────────

    @Test
    fun `get_sql_paging_rules result contains styles and offset`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_paging_rules")))
        assertTrue(result.containsKey("styles"))
        assertTrue(result.containsKey("offset"))
    }

    @Test
    fun `get_sql_paging_rules styles is a non-empty list`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_paging_rules")))
        @Suppress("UNCHECKED_CAST")
        val styles = result["styles"] as List<Map<String, Any?>>
        assertFalse(styles.isEmpty())
        assertTrue(styles.first().containsKey("syntax"))
        assertTrue(styles.first().containsKey("type"))
    }

    // ── get_sql_join_rules ────────────────────────────────────────────────────

    @Test
    fun `get_sql_join_rules result contains expected fields`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_join_rules")))
        assertTrue(result.containsKey("style"))
        assertTrue(result.containsKey("onClauseKeyword"))
        assertTrue(result.containsKey("joinTypes"))
    }

    @Test
    fun `get_sql_join_rules joinTypes contains INNER`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_join_rules")))
        @Suppress("UNCHECKED_CAST")
        val joinTypes = result["joinTypes"] as List<Map<String, Any?>>
        assertTrue(joinTypes.any { it["name"] == "INNER" })
    }

    // ── get_sql_functions ─────────────────────────────────────────────────────

    @Test
    fun `get_sql_functions returns function list for valid category`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_functions"),
            """{"category":"aggregates"}"""))
        assertTrue(result.containsKey("category"))
        assertTrue(result.containsKey("functions"))
        @Suppress("UNCHECKED_CAST")
        val functions = result["functions"] as List<Map<String, Any?>>
        assertFalse(functions.isEmpty())
        assertTrue(functions.any { it["name"] == "COUNT" })
    }

    @Test
    fun `get_sql_functions result functions contain name field`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_functions"),
            """{"category":"aggregates"}"""))
        @Suppress("UNCHECKED_CAST")
        val functions = result["functions"] as List<Map<String, Any?>>
        assertTrue(functions.all { it.containsKey("name") })
    }

    @Test
    fun `get_sql_functions returns error for unknown category`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_functions"),
            """{"category":"no_such_category"}"""))
        assertTrue(result.containsKey("error"))
        assertTrue(result.containsKey("availableCategories"))
    }

    @Test
    fun `get_sql_functions error result lists available categories`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_functions"),
            """{"category":"no_such_category"}"""))
        @Suppress("UNCHECKED_CAST")
        val categories = result["availableCategories"] as List<String>
        assertTrue(categories.contains("aggregates"))
    }

    // ── get_sql_function_info ─────────────────────────────────────────────────

    @Test
    fun `get_sql_function_info returns full detail for known function`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_function_info"),
            """{"name":"COUNT"}"""))
        assertTrue(result.containsKey("name"))
        assertTrue(result.containsKey("category"))
        assertTrue(result.containsKey("syntax"))
        assertTrue(result.containsKey("returnType"))
        assertTrue(result.containsKey("args"))
        assertEquals("COUNT", result["name"])
        assertEquals("aggregates", result["category"])
    }

    @Test
    fun `get_sql_function_info args field is a list`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_function_info"),
            """{"name":"COUNT"}"""))
        @Suppress("UNCHECKED_CAST")
        val args = result["args"] as List<Map<String, Any?>>
        assertFalse(args.isEmpty())
        assertTrue(args.first().containsKey("name"))
        assertTrue(args.first().containsKey("type"))
    }

    @Test
    fun `get_sql_function_info returns error for unknown function`() {
        val result = sim.parseMap(sim.invoke(tool("get_sql_function_info"),
            """{"name":"NO_SUCH_FUNCTION_XYZ"}"""))
        assertTrue(result.containsKey("error"))
        assertFalse(result.containsKey("category"))
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private fun tool(name: String): ToolDefinition =
        capability.tools.single { it.name == name }
}
