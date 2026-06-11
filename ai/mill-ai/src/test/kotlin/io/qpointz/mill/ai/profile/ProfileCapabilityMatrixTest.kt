package io.qpointz.mill.ai.profile

import io.qpointz.mill.ai.capabilities.metadata.EmptyMetadataReadPort
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.schema.ListColumnsItem
import io.qpointz.mill.ai.capabilities.schema.ListRelationsItem
import io.qpointz.mill.ai.capabilities.schema.ListSchemasItem
import io.qpointz.mill.ai.capabilities.schema.ListTablesItem
import io.qpointz.mill.ai.capabilities.schema.RelationDirection
import io.qpointz.mill.ai.capabilities.sqlquery.MockSqlValidationService
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.MockValueMappingResolver
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.dependencies.SchemaFacingCapabilityDependencyFactory
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProfileCapabilityMatrixTest {

    private val registry = CapabilityRegistry.load()
    private val catalog = object : SchemaCatalogPort {
        override fun listSchemas(): List<ListSchemasItem> = emptyList()
        override fun listTables(schemaName: String): List<ListTablesItem> = emptyList()
        override fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem> = emptyList()
        override fun listRelations(schemaName: String, tableName: String, direction: RelationDirection): List<ListRelationsItem> =
            emptyList()
    }
    private val dialect = DialectRegistry.fromClasspathDefaults().requireDialect("calcite")

    private fun contextFor(profile: AgentProfile) = AgentContext(
        contextType = "general",
        capabilityDependencies = SchemaFacingCapabilityDependencyFactory.build(
            profile = profile,
            schemaCatalog = catalog,
            metadataReadPort = EmptyMetadataReadPort(),
            dialectSpec = dialect,
            sqlQueryDependency = SqlQueryCapabilityDependency(MockSqlValidationService()),
            valueMappingResolver = MockValueMappingResolver(),
        ),
    )

    @Test
    fun shouldExcludeMetadataAuthoring_fromDataAnalysisProfile() {
        val profile = DefaultProfileRegistry.resolve("data-analysis")!!
        assertFalse(profile.capabilityIds.contains("metadata-authoring"))
    }

    @Test
    fun shouldIncludeMetadataAuthoring_inSchemaAuthoringProfile() {
        val profile = DefaultProfileRegistry.resolve("schema-authoring")!!
        assertTrue(profile.capabilityIds.contains("metadata-authoring"))
    }

    @Test
    fun shouldResolveValidateSql_forDataAnalysis_andNotProposeFacetAssignment() {
        val profile = DefaultProfileRegistry.resolve("data-analysis")!!
        val toolNames = registry.capabilitiesFor(profile, contextFor(profile))
            .flatMap { it.tools }
            .map { it.spec.name() }
            .toSet()
        assertTrue(toolNames.contains("validate_sql"))
        assertFalse(toolNames.contains("propose_facet_assignment"))
    }

    @Test
    fun shouldResolveProposeFacetAssignment_forSchemaAuthoring() {
        val profile = DefaultProfileRegistry.resolve("schema-authoring")!!
        val toolNames = registry.capabilitiesFor(profile, contextFor(profile))
            .flatMap { it.tools }
            .map { it.spec.name() }
            .toSet()
        assertTrue(toolNames.contains("propose_facet_assignment"))
    }
}
