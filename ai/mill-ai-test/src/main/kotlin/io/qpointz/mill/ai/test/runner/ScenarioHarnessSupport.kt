package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.ai.capabilities.schema.ListColumnsItem
import io.qpointz.mill.ai.capabilities.schema.ListRelationsItem
import io.qpointz.mill.ai.capabilities.schema.ListSchemasItem
import io.qpointz.mill.ai.capabilities.schema.ListTablesItem
import io.qpointz.mill.ai.capabilities.schema.RelationDirection
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.sqlquery.MockSqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.MockValueMappingResolver
import io.qpointz.mill.ai.dependencies.SchemaFacingCapabilityDependencyFactory
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.sql.v2.dialect.DialectRegistry

/**
 * Test collaborators for scenario packs that exercise schema-facing profiles.
 */
object ScenarioHarnessSupport {

    private val emptyCatalog = object : SchemaCatalogPort {
        override fun listSchemas(): List<ListSchemasItem> = emptyList()
        override fun listTables(schemaName: String): List<ListTablesItem> = emptyList()
        override fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem> = emptyList()
        override fun listRelations(schemaName: String, tableName: String, direction: RelationDirection): List<ListRelationsItem> =
            emptyList()
    }

    /** Metadata port with harness facet catalog (≥5 types per GAPS §12). */
    val metadataReadPort: MetadataReadPort = HarnessMetadataReadPort()

    private val dialectSpec = DialectRegistry.fromClasspathDefaults().requireDialect("calcite")

    /**
     * Builds an [AgentContext] with stub schema/metadata/SQL dependencies for [profile].
     *
     * @param profile Active scenario profile.
     */
    fun agentContext(profile: AgentProfile): AgentContext = AgentContext(
        contextType = "general",
        capabilityDependencies = SchemaFacingCapabilityDependencyFactory.build(
            profile = profile,
            schemaCatalog = emptyCatalog,
            metadataReadPort = metadataReadPort,
            dialectSpec = dialectSpec,
            sqlQueryDependency = SqlQueryCapabilityDependency(MockSqlValidationService()),
            valueMappingResolver = MockValueMappingResolver(),
        ),
    )
}
