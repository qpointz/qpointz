package io.qpointz.mill.ai.capabilities.chartmapping

/**
 * MVP chart catalog definitions for list_supported_charts and validate_chart_spec.
 */
object ChartCatalog {

    data class EncodingRole(
        val name: String,
        val required: Boolean,
        val compatibleTypes: Set<String>,
    )

    data class ChartTypeDefinition(
        val chartType: String,
        val title: String,
        val description: String,
        val encodingRoles: List<EncodingRole>,
        val options: Map<String, Any?>,
        val defaultLimit: Int,
        val hardLimit: Int,
    )

    private val numericTypes = setOf(
        "TINY_INT", "TINYINT",
        "SMALL_INT", "SMALLINT",
        "INT", "INTEGER",
        "BIG_INT", "BIGINT",
        "FLOAT", "REAL",
        "DOUBLE",
        "DECIMAL", "NUMERIC",
    )
    private val categoryTypes = setOf(
        "STRING", "VARCHAR", "CHAR", "TEXT",
        "BOOL", "BOOLEAN",
        "UUID", "DATE", "TIME", "TIMESTAMP", "TIMESTAMP_TZ", "TIMESTAMP_WITH_TIMEZONE",
    )

    val chartTypes: List<ChartTypeDefinition> = listOf(
        ChartTypeDefinition(
            chartType = "bar",
            title = "Bar chart",
            description = "Compare a numeric measure across categories.",
            encodingRoles = listOf(
                EncodingRole("category", required = true, compatibleTypes = categoryTypes),
                EncodingRole("value", required = true, compatibleTypes = numericTypes),
                EncodingRole("series", required = false, compatibleTypes = setOf("STRING", "BOOL", "UUID")),
            ),
            options = mapOf(
                "orientation" to listOf("vertical", "horizontal"),
                "stacked" to false,
            ),
            defaultLimit = 500,
            hardLimit = 5000,
        ),
        ChartTypeDefinition(
            chartType = "line",
            title = "Line chart",
            description = "Show trends over a continuous or ordered dimension.",
            encodingRoles = listOf(
                EncodingRole("x", required = true, compatibleTypes = categoryTypes + numericTypes),
                EncodingRole("y", required = true, compatibleTypes = numericTypes),
                EncodingRole("series", required = false, compatibleTypes = setOf("STRING", "BOOL", "UUID")),
            ),
            options = mapOf("smooth" to false),
            defaultLimit = 500,
            hardLimit = 5000,
        ),
        ChartTypeDefinition(
            chartType = "area",
            title = "Area chart",
            description = "Show cumulative or filled trends over an ordered dimension.",
            encodingRoles = listOf(
                EncodingRole("x", required = true, compatibleTypes = categoryTypes + numericTypes),
                EncodingRole("y", required = true, compatibleTypes = numericTypes),
                EncodingRole("series", required = false, compatibleTypes = setOf("STRING", "BOOL", "UUID")),
            ),
            options = mapOf("stacked" to false, "smooth" to false),
            defaultLimit = 500,
            hardLimit = 5000,
        ),
        ChartTypeDefinition(
            chartType = "scatter",
            title = "Scatter chart",
            description = "Plot two numeric measures per observation.",
            encodingRoles = listOf(
                EncodingRole("x", required = true, compatibleTypes = numericTypes),
                EncodingRole("y", required = true, compatibleTypes = numericTypes),
                EncodingRole("series", required = false, compatibleTypes = setOf("STRING", "BOOL", "UUID")),
                EncodingRole("color", required = false, compatibleTypes = categoryTypes),
            ),
            options = emptyMap(),
            defaultLimit = 2000,
            hardLimit = 5000,
        ),
        ChartTypeDefinition(
            chartType = "pie",
            title = "Pie chart",
            description = "Show part-to-whole composition for a category and measure.",
            encodingRoles = listOf(
                EncodingRole("category", required = true, compatibleTypes = categoryTypes),
                EncodingRole("value", required = true, compatibleTypes = numericTypes),
            ),
            options = mapOf("donut" to false),
            defaultLimit = 50,
            hardLimit = 200,
        ),
    )

    /** @param chartType chart type key */
    fun definition(chartType: String): ChartTypeDefinition? =
        chartTypes.firstOrNull { it.chartType == chartType.trim().lowercase() }
}
