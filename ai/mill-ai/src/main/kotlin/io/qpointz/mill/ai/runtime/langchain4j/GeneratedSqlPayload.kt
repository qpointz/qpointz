package io.qpointz.mill.ai.runtime.langchain4j

import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectCapabilityDependency
import io.qpointz.mill.ai.runtime.AgentContext

/**
 * Builds the nested WI-366 `generated-sql` artifact payload from coordinator draft state.
 */
object GeneratedSqlPayload {

    /**
     * Mutable turn-scoped draft assembled by [SqlArtifactCompletionCoordinator].
     *
     * @param sql nested SQL section
     * @param info human context (`title`, `description`)
     * @param schema trusted result schema from `describe_sql`
     * @param visualizations chart configs keyed by visualization `key`
     * @param profiling reserved profiling section
     */
    data class Draft(
        val sql: MutableMap<String, Any?> = mutableMapOf(),
        val info: MutableMap<String, String> = mutableMapOf(),
        var schema: List<Map<String, Any?>> = emptyList(),
        val visualizations: MutableMap<String, Map<String, Any?>> = mutableMapOf(),
        var profiling: List<Any?> = emptyList(),
    ) {
        /** @return immutable nested payload map for protocol final emission */
        fun toPayloadMap(): Map<String, Any?> = buildMap {
            put("artifactType", "generated-sql")
            put("sql", sql.toMap())
            put("info", info.toMap())
            if (schema.isNotEmpty()) put("schema", schema)
            if (visualizations.isNotEmpty()) put("visualizations", visualizations.values.toList())
            if (profiling.isNotEmpty()) put("profiling", profiling)
        }
    }

    /**
     * Seeds a new draft from a successful `validate_sql` result.
     *
     * @param normalizedSql validated SQL text
     * @param title human title from tool input
     * @param description human description from tool input
     * @param context agent context for dialect resolution
     */
    fun seedFromValidation(
        normalizedSql: String,
        title: String,
        description: String,
        context: AgentContext,
    ): Draft {
        val sqlSection = mutableMapOf<String, Any?>(
            "text" to normalizedSql,
            "dialectId" to resolveDialectId(context),
            "statementKind" to inferStatementKind(normalizedSql),
            "source" to "generated",
            "validationWarnings" to emptyList<String>(),
        )
        return Draft(
            sql = sqlSection,
            info = mutableMapOf("title" to title.trim(), "description" to description.trim()),
        )
    }

    /**
     * Loads an existing persisted payload into a draft for `enrich-existing` plans.
     *
     * @param inner normalized inner payload from artifact store
     */
    @Suppress("UNCHECKED_CAST")
    fun fromPersisted(inner: Map<String, Any?>): Draft {
        val sqlSection: MutableMap<String, Any?> = when (val sql = inner["sql"]) {
            is Map<*, *> -> sql.entries.associate { (k, v) -> k.toString() to v }.toMutableMap()
            is String -> mutableMapOf(
                "text" to sql,
                "dialectId" to (inner["dialectId"] ?: "unknown"),
                "statementKind" to inferStatementKind(sql),
                "source" to (inner["source"] ?: "generated"),
                "validationWarnings" to (inner["validationWarnings"] ?: emptyList<String>()),
            )
            else -> mutableMapOf()
        }
        val infoSection = when (val info = inner["info"]) {
            is Map<*, *> -> info.entries
                .mapNotNull { (k, v) -> (v as? String)?.let { k.toString() to it } }
                .toMap()
                .toMutableMap<String, String>()
            else -> mutableMapOf<String, String>()
        }
        val schema = (inner["schema"] as? List<*>)?.mapNotNull { row ->
            (row as? Map<*, *>)?.entries?.associate { (k, v) -> k.toString() to v }
        } ?: emptyList()
        val visualizations = mutableMapOf<String, Map<String, Any?>>()
        (inner["visualizations"] as? List<*>)?.forEach { entry ->
            val map = entry as? Map<*, *> ?: return@forEach
            val key = map["key"]?.toString() ?: return@forEach
            @Suppress("UNCHECKED_CAST")
            visualizations[key] = map.entries.associate { (k, v) -> k.toString() to v }
        }
        return Draft(
            sql = sqlSection,
            info = infoSection,
            schema = schema,
            visualizations = visualizations,
            profiling = (inner["profiling"] as? List<*>)?.filterNotNull() ?: emptyList(),
        )
    }

    private fun resolveDialectId(context: AgentContext): String =
        context.capabilityDependencies
            .forCapability("sql-dialect")
            ?.get(SqlDialectCapabilityDependency::class.java)
            ?.dialectSpec
            ?.id
            ?: "unknown"

    private fun inferStatementKind(sql: String): String =
        when {
            sql.trimStart().startsWith("SELECT", ignoreCase = true) -> "select"
            sql.trimStart().startsWith("WITH", ignoreCase = true) -> "select"
            else -> "unknown"
        }
}
