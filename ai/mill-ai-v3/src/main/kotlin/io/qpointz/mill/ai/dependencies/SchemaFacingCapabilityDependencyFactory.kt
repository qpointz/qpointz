package io.qpointz.mill.ai.dependencies

import io.qpointz.mill.ai.capabilities.schema.SchemaCapabilityDependency
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectCapabilityDependency
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingResolver
import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityDependencyContainer
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec

/**
 * Single place to construct [CapabilityDependencyContainer] entries for schema-facing
 * capabilities (`schema`, `sql-dialect`, `sql-query`, `value-mapping`).
 *
 * Used by in-process agents (e.g. [io.qpointz.mill.ai.runtime.langchain4j.SchemaExplorationAgent]) and
 * by server-side assemblers so capability id strings and dependency typings stay aligned.
 */
object SchemaFacingCapabilityDependencyFactory {

    /** Capability id for the read-only schema exploration tools. */
    const val SCHEMA = "schema"

    /** Capability id for SQL dialect metadata tools. */
    const val SQL_DIALECT = "sql-dialect"

    /** Capability id for SQL validate / generated-SQL flows. */
    const val SQL_QUERY = "sql-query"

    /** Capability id for value normalization before SQL. */
    const val VALUE_MAPPING = "value-mapping"

    /**
     * Builds a [CapabilityDependencyContainer] for [profile], filling slots only for capability
     * ids present on [profile] that have optional collaborators supplied.
     *
     * Missing collaborators for a capability id on the profile yield an incomplete
     * [io.qpointz.mill.ai.core.capability.CapabilityDependencies] map for that id; downstream
     * [io.qpointz.mill.ai.core.capability.CapabilityRegistry.validateDependencies] fails on send.
     *
     * @param profile Resolved profile whose [AgentProfile.capabilityIds] drive which entries are emitted.
     * @param schemaCatalog Required when [SCHEMA] is in the profile.
     * @param dialectSpec Required when [SQL_DIALECT] is in the profile.
     * @param sqlQueryDependency Required when [SQL_QUERY] is in the profile.
     * @param valueMappingResolver Required when [VALUE_MAPPING] is in the profile (hosts may pass a stub).
     */
    fun build(
        profile: AgentProfile,
        schemaCatalog: SchemaCatalogPort?,
        dialectSpec: SqlDialectSpec?,
        sqlQueryDependency: SqlQueryCapabilityDependency?,
        valueMappingResolver: ValueMappingResolver?,
    ): CapabilityDependencyContainer {
        val ids = profile.capabilityIds
        val entries = mutableListOf<Pair<String, CapabilityDependencies>>()
        if (SCHEMA in ids && schemaCatalog != null) {
            entries += SCHEMA to CapabilityDependencies.of(SchemaCapabilityDependency(schemaCatalog))
        }
        if (SQL_DIALECT in ids && dialectSpec != null) {
            entries += SQL_DIALECT to CapabilityDependencies.of(SqlDialectCapabilityDependency(dialectSpec))
        }
        if (SQL_QUERY in ids && sqlQueryDependency != null) {
            entries += SQL_QUERY to CapabilityDependencies.of(sqlQueryDependency)
        }
        if (VALUE_MAPPING in ids && valueMappingResolver != null) {
            entries += VALUE_MAPPING to CapabilityDependencies.of(ValueMappingCapabilityDependency(valueMappingResolver))
        }
        return CapabilityDependencyContainer.of(*entries.toTypedArray())
    }
}
