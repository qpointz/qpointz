package io.qpointz.mill.ai.dependencies

import io.qpointz.mill.ai.capabilities.metadata.MetadataCapabilityDependency
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
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
 * Constructs [CapabilityDependencyContainer] entries for host-backed capabilities
 * (`schema`, `metadata`, `metadata-authoring`, SQL, value-mapping).
 */
object SchemaFacingCapabilityDependencyFactory {

    const val SCHEMA = "schema"

    const val METADATA = "metadata"

    const val METADATA_AUTHORING = "metadata-authoring"

    const val SQL_DIALECT = "sql-dialect"

    const val SQL_QUERY = "sql-query"

    const val VALUE_MAPPING = "value-mapping"

    fun build(
        profile: AgentProfile,
        schemaCatalog: SchemaCatalogPort?,
        metadataReadPort: MetadataReadPort?,
        dialectSpec: SqlDialectSpec?,
        sqlQueryDependency: SqlQueryCapabilityDependency?,
        valueMappingResolver: ValueMappingResolver?,
    ): CapabilityDependencyContainer {
        val ids = profile.capabilityIds
        val entries = mutableListOf<Pair<String, CapabilityDependencies>>()
        if (SCHEMA in ids && schemaCatalog != null) {
            entries += SCHEMA to CapabilityDependencies.of(SchemaCapabilityDependency(schemaCatalog))
        }
        if (METADATA in ids && metadataReadPort != null) {
            val dep = MetadataCapabilityDependency(metadataReadPort)
            entries += METADATA to CapabilityDependencies.of(dep)
        }
        if (METADATA_AUTHORING in ids && metadataReadPort != null) {
            val dep = MetadataCapabilityDependency(metadataReadPort)
            entries += METADATA_AUTHORING to CapabilityDependencies.of(dep)
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
        /** Schema authoring captures must resolve entity ids via the same catalog as `list_tables` / `list_columns`. */
        if ("schema-authoring" in ids && schemaCatalog != null) {
            entries += "schema-authoring" to CapabilityDependencies.of(SchemaCapabilityDependency(schemaCatalog))
        }
        return CapabilityDependencyContainer.of(*entries.toTypedArray())
    }
}
