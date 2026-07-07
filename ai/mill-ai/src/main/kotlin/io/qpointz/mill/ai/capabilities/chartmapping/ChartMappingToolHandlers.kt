package io.qpointz.mill.ai.capabilities.chartmapping

/**
 * Pure stateless chart-mapping tool implementations.
 */
object ChartMappingToolHandlers {

    data class SupportedChartsArtifact(
        val artifactType: String = "chart-catalog",
        val encodingContract: Map<String, Any?>,
        val charts: List<Map<String, Any?>>,
    )

    data class ChartValidationArtifact(
        val artifactType: String = "chart-validation",
        val passed: Boolean,
        val code: String? = null,
        val message: String? = null,
        val normalizedVisualization: Map<String, Any?>? = null,
        val targetArtifactId: String? = null,
        val opensEnrichPlan: Boolean = false,
        /** Sorted column names from the trusted schema passed into validation (plan routing). */
        val schemaColumnNames: List<String> = emptyList(),
    )

    /**
     * Returns the MVP chart catalog for model grounding.
     *
     * @param chartType optional filter to return one chart type entry (encoding roles, options, examples)
     */
    fun listSupportedCharts(chartType: String? = null): SupportedChartsArtifact {
        val definitions = ChartCatalog.chartTypes.filter { definition ->
            chartType.isNullOrBlank() || definition.chartType == chartType.trim().lowercase()
        }
        return SupportedChartsArtifact(
            encodingContract = encodingContract(),
            charts = definitions.map { def -> chartCatalogEntry(def) },
        )
    }

    private fun encodingContract(): Map<String, Any?> = mapOf(
        "valueShape" to mapOf(
            "field" to "Required. Must match a column name from trusted describe_sql schema[].",
            "label" to "Optional human-readable axis/legend label.",
        ),
        "notes" to listOf(
            "Each encoding role is an object with at least a field key — not a bare column string.",
            "Field names must exist in the schema[] passed to validate_chart_spec.",
            "Use the requiredEncodings and exampleEncodings for the chosen chartType from this catalog.",
        ),
    )

    private fun chartCatalogEntry(def: ChartCatalog.ChartTypeDefinition): Map<String, Any?> {
        val required = def.encodingRoles.filter { it.required }
        val optional = def.encodingRoles.filterNot { it.required }
        return mapOf(
            "chartType" to def.chartType,
            "title" to def.title,
            "description" to def.description,
            "requiredEncodings" to required.map { role -> encodingRoleEntry(role) },
            "optionalEncodings" to optional.map { role -> encodingRoleEntry(role) },
            "exampleEncodings" to exampleEncodings(def),
            "options" to def.options,
            "snapshotLimits" to mapOf(
                "defaultLimit" to def.defaultLimit,
                "hardLimit" to def.hardLimit,
            ),
        )
    }

    private fun encodingRoleEntry(role: ChartCatalog.EncodingRole): Map<String, Any?> =
        mapOf(
            "name" to role.name,
            "required" to role.required,
            "compatibleTypes" to role.compatibleTypes.toList(),
        )

    private fun exampleEncodings(def: ChartCatalog.ChartTypeDefinition): Map<String, Map<String, String>> =
        def.encodingRoles
            .filter { it.required }
            .associate { role ->
                role.name to mapOf("field" to encodingPlaceholder(role.name))
            }

    private fun encodingPlaceholder(roleName: String): String =
        when (roleName) {
            "category", "x" -> "<category-or-dimension-column>"
            "value", "y" -> "<numeric-measure-column>"
            else -> "<column>"
        }

    /**
     * Validates a semantic chart visualization config against trusted schema and catalog rules.
     *
     * @param schema trusted result schema from describe_sql
     * @param chartType requested chart type
     * @param encodings semantic encoding roles
     * @param key visualization key within the SQL artifact
     * @param title optional chart title
     * @param description optional chart description
     * @param options chart-type options
     * @param presentation presentation hints
     * @param targetArtifactId optional existing sql.generated artifact id for enrich-existing
     * @param enrichExisting when true, signals enrich-existing on last-sql without an explicit artifact id
     */
    @Suppress("UNCHECKED_CAST")
    fun validateChartSpec(
        schema: List<Map<String, Any?>>,
        chartType: String,
        encodings: Map<String, Any?>,
        key: String = "default",
        title: String? = null,
        description: String? = null,
        options: Map<String, Any?>? = null,
        presentation: Map<String, Any?>? = null,
        targetArtifactId: String? = null,
        enrichExisting: Boolean = false,
    ): ChartValidationArtifact {
        val opensEnrich = !targetArtifactId.isNullOrBlank() || enrichExisting
        val columnNames = sortedSchemaColumnNames(schema)
        val definition = ChartCatalog.definition(chartType)
            ?: return ChartValidationArtifact(
                passed = false,
                code = "unsupported_chart_type",
                message = "Unsupported chart type: $chartType",
                targetArtifactId = targetArtifactId,
                opensEnrichPlan = opensEnrich,
                schemaColumnNames = columnNames,
            )
        if (schema.isEmpty()) {
            return ChartValidationArtifact(
                passed = false,
                code = "schema_required",
                message = "Trusted schema is required before chart validation. Call describe_sql first.",
                targetArtifactId = targetArtifactId,
                opensEnrichPlan = opensEnrich,
                schemaColumnNames = columnNames,
            )
        }
        val normalizedEncodingsInput = normalizeEncodings(definition.chartType, encodings)
        if (containsRendererConfig(normalizedEncodingsInput) || containsRendererConfig(options) || containsRendererConfig(presentation)) {
            return ChartValidationArtifact(
                passed = false,
                code = "renderer_config_forbidden",
                message = "Renderer-specific configuration is not allowed in durable chart visualizations.",
                targetArtifactId = targetArtifactId,
                opensEnrichPlan = opensEnrich,
                schemaColumnNames = columnNames,
            )
        }
        val schemaByName = schema.associateBy { it["name"]?.toString().orEmpty() }
        definition.encodingRoles.filter { it.required }.forEach { role ->
            val encoding = normalizedEncodingsInput[role.name]
            val field = encoding?.get("field")?.toString()?.trim().orEmpty()
            if (field.isBlank()) {
                return ChartValidationArtifact(
                    passed = false,
                    code = "missing_encoding",
                    message = "Required encoding '${role.name}' is missing.",
                    targetArtifactId = targetArtifactId,
                    opensEnrichPlan = opensEnrich,
                    schemaColumnNames = columnNames,
                )
            }
            val column = resolveSchemaColumn(schemaByName, field)
            if (column == null) {
                return ChartValidationArtifact(
                    passed = false,
                    code = "unknown_field",
                    message = "Encoding field '$field' is not present in schema.",
                    targetArtifactId = targetArtifactId,
                    opensEnrichPlan = opensEnrich,
                    schemaColumnNames = columnNames,
                )
            }
            val columnType = column["type"]?.toString()?.uppercase().orEmpty()
            if (columnType !in role.compatibleTypes) {
                return ChartValidationArtifact(
                    passed = false,
                    code = "incompatible_type",
                    message = "Field '$field' type '$columnType' is incompatible with role '${role.name}'.",
                    targetArtifactId = targetArtifactId,
                    opensEnrichPlan = opensEnrich,
                    schemaColumnNames = columnNames,
                )
            }
        }
        val valueRole = if (definition.chartType == "pie" || definition.chartType == "bar") "value" else "y"
        val categoryRole = if (definition.chartType == "pie" || definition.chartType == "bar") "category" else "x"
        val categoryField = normalizedEncodingsInput[categoryRole]?.get("field")?.toString()
        val valueField = normalizedEncodingsInput[valueRole]?.get("field")?.toString()
        if (categoryField != null && valueField != null && categoryField == valueField) {
            return ChartValidationArtifact(
                passed = false,
                code = "query_refinement_needed",
                message = "Category and value cannot use the same field. Refine the SQL result shape.",
                targetArtifactId = targetArtifactId,
                opensEnrichPlan = opensEnrich,
                schemaColumnNames = columnNames,
            )
        }
        val normalizedEncodings = normalizedEncodingsInput.mapValues { (_, value) ->
            val field = value["field"]?.toString().orEmpty()
            val resolvedField = resolveSchemaColumn(schemaByName, field)?.get("name")?.toString() ?: field
            buildMap<String, Any?> {
                put("field", resolvedField)
                value["label"]?.toString()?.takeIf { it.isNotBlank() }?.let { put("label", it) }
            }
        }
        val visualization = buildMap<String, Any?> {
            put("key", key.ifBlank { "default" })
            put("kind", "chart")
            title?.takeIf { it.isNotBlank() }?.let { put("title", it.trim()) }
            description?.takeIf { it.isNotBlank() }?.let { put("description", it.trim()) }
            put("chartType", definition.chartType)
            put("encodings", normalizedEncodings)
            options?.takeIf { it.isNotEmpty() }?.let { put("options", it) }
            presentation?.takeIf { it.isNotEmpty() }?.let { put("presentation", it) }
        }
        return ChartValidationArtifact(
            passed = true,
            code = "ok",
            message = null,
            normalizedVisualization = visualization,
            targetArtifactId = targetArtifactId,
            opensEnrichPlan = opensEnrich,
            schemaColumnNames = columnNames,
        )
    }

    private fun sortedSchemaColumnNames(schema: List<Map<String, Any?>>): List<String> =
        schema.mapNotNull { it["name"]?.toString()?.trim()?.takeIf(String::isNotBlank) }.sorted()

    private fun normalizeEncodings(chartType: String, raw: Map<String, Any?>): Map<String, Map<String, Any?>> {
        val coerced = raw.mapValues { (_, value) -> coerceEncodingValue(value) }.toMutableMap()
        if (chartType == "bar" || chartType == "pie") {
            moveEncodingAlias(coerced, from = "x", to = "category")
            moveEncodingAlias(coerced, from = "y", to = "value")
            moveEncodingAlias(coerced, from = "dimension", to = "category")
            moveEncodingAlias(coerced, from = "measure", to = "value")
        } else {
            moveEncodingAlias(coerced, from = "category", to = "x")
            moveEncodingAlias(coerced, from = "value", to = "y")
        }
        return coerced.filterValues { it.isNotEmpty() }
    }

    private fun moveEncodingAlias(
        encodings: MutableMap<String, Map<String, Any?>>,
        from: String,
        to: String,
    ) {
        if (encodings.containsKey(to) || !encodings.containsKey(from)) return
        encodings[to] = encodings.remove(from) ?: return
    }

    private fun coerceEncodingValue(value: Any?): Map<String, Any?> {
        when (value) {
            is String -> {
                val field = value.trim()
                return if (field.isBlank()) emptyMap() else mapOf("field" to field)
            }
            is Map<*, *> -> {
                val map = value.entries.associate { it.key.toString() to it.value }
                val field = map["field"]?.toString()?.trim()
                    ?: map["column"]?.toString()?.trim()
                    ?: map["name"]?.toString()?.trim()
                if (field.isNullOrBlank()) return emptyMap()
                return buildMap {
                    put("field", field)
                    map["label"]?.toString()?.takeIf { it.isNotBlank() }?.let { put("label", it) }
                }
            }
        }
        return emptyMap()
    }

    private fun resolveSchemaColumn(
        schemaByName: Map<String, Map<String, Any?>>,
        field: String,
    ): Map<String, Any?>? =
        schemaByName[field]
            ?: schemaByName.entries.firstOrNull { it.key.equals(field, ignoreCase = true) }?.value

    private fun containsRendererConfig(value: Any?): Boolean {
        if (value !is Map<*, *>) return false
        val forbidden = setOf("echarts", "vega", "plotly", "series", "xAxis", "yAxis", "dataset")
        return value.keys.any { key -> forbidden.contains(key.toString().lowercase()) }
    }
}
