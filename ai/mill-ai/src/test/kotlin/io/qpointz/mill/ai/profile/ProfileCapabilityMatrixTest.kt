package io.qpointz.mill.ai.profile

import io.qpointz.mill.ai.capabilities.concept.EmptyConceptCatalogPort
import io.qpointz.mill.ai.capabilities.metadata.EmptyMetadataReadPort
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.schema.ListColumnsItem
import io.qpointz.mill.ai.capabilities.schema.ListRelationsItem
import io.qpointz.mill.ai.capabilities.schema.ListSchemasItem
import io.qpointz.mill.ai.capabilities.schema.ListTablesItem
import io.qpointz.mill.ai.capabilities.schema.RelationDirection
import io.qpointz.mill.ai.capabilities.sqlquery.mockSqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.MockValueMappingResolver
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.dependencies.SchemaFacingCapabilityDependencyFactory
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProfileCapabilityMatrixTest {

    private val registry = CapabilityRegistry.load()
    private val profiles = PlatformProfiles.registry()
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
            conceptCatalog = EmptyConceptCatalogPort,
            dialectSpec = dialect,
            sqlQueryDependency = mockSqlQueryCapabilityDependency(),
            valueMappingResolver = MockValueMappingResolver(),
        ),
    )

    @Test
    fun shouldIncludeMetadataAuthoring_inDataAnalysisProfile() {
        val profile = profiles.resolve("data-analysis")!!
        assertTrue(profile.capabilityIds.contains("metadata-authoring"))
    }

    @Test
    fun shouldIncludeMetadataAuthoring_inMetadataAuthoringProfile() {
        val profile = profiles.resolve("metadata-authoring")!!
        assertTrue(profile.capabilityIds.contains("metadata-authoring"))
    }

    @Test
    fun shouldIncludeConcept_inDataAnalysisProfile() {
        val profile = profiles.resolve("data-analysis")!!
        assertTrue(profile.capabilityIds.contains("concept"))
    }

    @Test
    fun shouldResolveConceptQueryTools_forDataAnalysis() {
        val profile = profiles.resolve("data-analysis")!!
        val toolNames = registry.capabilitiesFor(profile, contextFor(profile))
            .flatMap { it.tools }
            .map { it.spec.name() }
            .toSet()
        assertTrue(toolNames.contains("search_concepts"))
        assertTrue(toolNames.contains("get_model_concepts"))
        assertTrue(toolNames.contains("validate_sql"))
    }

    @Test
    fun shouldResolveValidateSql_andProposeFacet_forDataAnalysis() {
        val profile = profiles.resolve("data-analysis")!!
        val toolNames = registry.capabilitiesFor(profile, contextFor(profile))
            .flatMap { it.tools }
            .map { it.spec.name() }
            .toSet()
        assertTrue(toolNames.contains("validate_sql"))
        assertTrue(toolNames.contains("describe_sql"))
        assertTrue(toolNames.contains("execute_sql"))
        assertTrue(toolNames.contains("list_supported_charts"))
        assertTrue(toolNames.contains("validate_chart_spec"))
        assertTrue(toolNames.contains("propose_facet_assignment"))
    }

    @Test
    fun shouldExcludeSqlQueryTools_fromSchemaExplorationProfile() {
        val profile = profiles.resolve("schema-exploration")!!
        assertThat(profile.capabilityIds).doesNotContain("sql-query")
        val toolNames = registry.capabilitiesFor(profile, contextFor(profile))
            .flatMap { it.tools }
            .map { it.spec.name() }
            .toSet()
        assertThat(toolNames).noneMatch { it in setOf("validate_sql", "describe_sql", "execute_sql") }
    }

    @Test
    fun shouldResolveProposeFacetAssignment_forMetadataAuthoring() {
        val profile = profiles.resolve("metadata-authoring")!!
        val toolNames = registry.capabilitiesFor(profile, contextFor(profile))
            .flatMap { it.tools }
            .map { it.spec.name() }
            .toSet()
        assertTrue(toolNames.contains("propose_facet_assignment"))
        assertFalse(toolNames.contains("validate_sql"))
    }
}
