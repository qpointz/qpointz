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
import io.qpointz.mill.metadata.domain.facet.FacetPayloadField
import io.qpointz.mill.metadata.domain.facet.FacetPayloadSchema
import io.qpointz.mill.metadata.domain.facet.FacetSchemaType
import io.qpointz.mill.metadata.domain.facet.FacetTypeManifest
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

    /** Metadata port with a single `descriptive` facet type for capture scenarios. */
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

/** Minimal metadata catalog for harness facet capture packs. */
class HarnessMetadataReadPort : MetadataReadPort {

    private val descriptiveFacet = FacetTypeManifest(
        typeKey = "descriptive",
        title = "Descriptive",
        description = "Short descriptive facet for harness tests",
        payload = FacetPayloadSchema(
            type = FacetSchemaType.OBJECT,
            title = "Descriptive payload",
            description = "Summary text",
            fields = listOf(
                FacetPayloadField(
                    name = "summary",
                    required = true,
                    schema = FacetPayloadSchema(
                        type = FacetSchemaType.STRING,
                        title = "Summary",
                        description = "Summary text",
                    ),
                ),
            ),
        ),
    )

    override fun listFacetTypes(): List<FacetTypeManifest> = listOf(descriptiveFacet)

    override fun listEntityFacets(
        metadataEntityId: String,
        scope: String?,
        context: String?,
        origin: String?,
    ): List<Map<String, Any?>> = emptyList()

    override fun validateFacetPayload(facetTypeKey: String, payload: Map<String, Any?>): List<String> =
        emptyList()
}
