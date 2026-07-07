package io.qpointz.mill.ai.capabilities.chartmapping

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ChartMappingToolHandlersTest {

    private val schema = listOf(
        mapOf("name" to "country", "type" to "STRING"),
        mapOf("name" to "client_count", "type" to "BIG_INT"),
    )

    @Test
    fun shouldListMvpChartTypes() {
        val catalog = ChartMappingToolHandlers.listSupportedCharts()
        assertEquals(5, catalog.charts.size)
        assertTrue(catalog.charts.any { it["chartType"] == "bar" })
        assertTrue(catalog.encodingContract.containsKey("valueShape"))
    }

    @Test
    fun shouldFilterCatalogByChartType() {
        val catalog = ChartMappingToolHandlers.listSupportedCharts(chartType = "bar")
        assertEquals(1, catalog.charts.size)
        @Suppress("UNCHECKED_CAST")
        val required = catalog.charts.single()["requiredEncodings"] as List<Map<String, Any?>>
        assertTrue(required.any { it["name"] == "category" })
        assertTrue(required.any { it["name"] == "value" })
        @Suppress("UNCHECKED_CAST")
        val examples = catalog.charts.single()["exampleEncodings"] as Map<String, Map<String, String>>
        assertTrue(examples.containsKey("category"))
        assertTrue(examples.containsKey("value"))
    }

    @Test
    fun shouldAcceptValidBarChartSpec() {
        val result = ChartMappingToolHandlers.validateChartSpec(
            schema = schema,
            chartType = "bar",
            encodings = mapOf(
                "category" to mapOf("field" to "country", "label" to "Country"),
                "value" to mapOf("field" to "client_count", "label" to "Clients"),
            ),
        )
        assertTrue(result.passed)
        assertEquals("chart", result.normalizedVisualization?.get("kind"))
    }

    @Test
    fun shouldAcceptCommonSqlTypeNamesForCountChart() {
        val result = ChartMappingToolHandlers.validateChartSpec(
            schema = listOf(
                mapOf("name" to "country", "type" to "VARCHAR"),
                mapOf("name" to "client_count", "type" to "BIGINT"),
            ),
            chartType = "bar",
            encodings = mapOf(
                "category" to mapOf("field" to "country", "label" to "Country"),
                "value" to mapOf("field" to "client_count", "label" to "Clients"),
            ),
        )
        assertTrue(result.passed)
        assertEquals("chart", result.normalizedVisualization?.get("kind"))
    }

    @Test
    fun shouldAcceptBarChartWithXYRoleAliases() {
        val result = ChartMappingToolHandlers.validateChartSpec(
            schema = schema,
            chartType = "bar",
            encodings = mapOf(
                "x" to mapOf("field" to "country"),
                "y" to mapOf("field" to "client_count"),
            ),
        )
        assertTrue(result.passed)
        @Suppress("UNCHECKED_CAST")
        val encodings = result.normalizedVisualization?.get("encodings") as Map<String, Map<String, Any?>>
        assertEquals("country", encodings["category"]?.get("field"))
        assertEquals("client_count", encodings["value"]?.get("field"))
    }

    @Test
    fun shouldAcceptStringShorthandEncodings() {
        val result = ChartMappingToolHandlers.validateChartSpec(
            schema = schema,
            chartType = "bar",
            encodings = mapOf(
                "category" to "country",
                "value" to "client_count",
            ),
        )
        assertTrue(result.passed)
    }

    @Test
    fun shouldResolveSchemaFieldCaseInsensitively() {
        val result = ChartMappingToolHandlers.validateChartSpec(
            schema = listOf(
                mapOf("name" to "Country", "type" to "STRING"),
                mapOf("name" to "CLIENT_COUNT", "type" to "BIGINT"),
            ),
            chartType = "bar",
            encodings = mapOf(
                "category" to mapOf("field" to "country"),
                "value" to mapOf("field" to "client_count"),
            ),
        )
        assertTrue(result.passed)
        @Suppress("UNCHECKED_CAST")
        val encodings = result.normalizedVisualization?.get("encodings") as Map<String, Map<String, Any?>>
        assertEquals("Country", encodings["category"]?.get("field"))
        assertEquals("CLIENT_COUNT", encodings["value"]?.get("field"))
    }

    @Test
    fun shouldRejectUnknownField() {
        val result = ChartMappingToolHandlers.validateChartSpec(
            schema = schema,
            chartType = "bar",
            encodings = mapOf(
                "category" to mapOf("field" to "missing"),
                "value" to mapOf("field" to "client_count"),
            ),
        )
        assertFalse(result.passed)
        assertEquals("unknown_field", result.code)
    }

    @Test
    fun shouldRejectRendererSpecificConfig() {
        val result = ChartMappingToolHandlers.validateChartSpec(
            schema = schema,
            chartType = "bar",
            encodings = mapOf(
                "category" to mapOf("field" to "country"),
                "value" to mapOf("field" to "client_count"),
            ),
            options = mapOf("echarts" to mapOf("series" to emptyList<Any>())),
        )
        assertFalse(result.passed)
        assertEquals("renderer_config_forbidden", result.code)
    }
}
