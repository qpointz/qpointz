package io.qpointz.mill.sql.v2.dialect

import java.util.Locale

class DialectRegistry(
    dialects: Map<String, SqlDialectSpec>
) {
    companion object {
        @JvmField
        val DEFAULT_RESOURCE_DIALECTS: Set<String> = setOf("calcite", "h2", "mysql", "postgres")

        @JvmStatic
        fun fromClasspathDefaults(): DialectRegistry {
            val loader = DialectLoader()
            val loaded = DEFAULT_RESOURCE_DIALECTS
                .map { loader.loadFromClasspath(it) }
                .associateBy { it.id.uppercase(Locale.ROOT) }
            return DialectRegistry(loaded)
        }
    }

    private val byId: Map<String, SqlDialectSpec> = dialects.values.associateBy { it.id.uppercase(Locale.ROOT) }

    fun getDialect(dialectId: String?): SqlDialectSpec? =
        dialectId?.let { byId[it.uppercase(Locale.ROOT)] }

    fun requireDialect(dialectId: String): SqlDialectSpec =
        getDialect(dialectId) ?: throw DialectValidationException(
            message = "Unknown dialect: $dialectId",
            errors = listOf("dialect not found in registry")
        )

    fun allDialects(): List<SqlDialectSpec> = byId.values.toList()

    fun ids(): Set<String> = byId.keys

    fun size(): Int = byId.size
}
