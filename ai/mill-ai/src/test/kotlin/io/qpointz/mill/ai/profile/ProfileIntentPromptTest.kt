package io.qpointz.mill.ai.profile

import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.dependencies.SchemaFacingCapabilityDependencyFactory
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
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.langchain4j.buildAgentSystemPrompt
import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProfileIntentPromptTest {

    private val registry = CapabilityRegistry.load()
    private val profiles = PlatformProfiles.registry()

    @Test
    fun shouldTrimMetadataAuthoringIntent_toCapabilityLocalRoutes() {
        val intent = CapabilityManifest.load("capabilities/metadata-authoring.yaml")
            .promptAsset("metadata-authoring.intent")
        assertTrue(intent.content.contains("AUTHOR_FACET"))
        assertFalse(intent.content.contains("DATA_QUERY"))
        assertFalse(intent.content.contains("EXPLORE"))
    }

    @Test
    fun shouldDeclareSqlQueryIntent_forDataRetrieval() {
        val intent = CapabilityManifest.load("capabilities/sql-query.yaml")
            .promptAsset("sql-query.intent")
        assertTrue(intent.content.contains("DATA_QUERY"))
    }

    @Test
    fun shouldDeclareSchemaIntent_forExploration() {
        val intent = CapabilityManifest.load("capabilities/schema.yaml")
            .promptAsset("schema.intent")
        assertTrue(intent.content.contains("EXPLORE"))
    }

    @Test
    fun shouldLoadDataAnalysisProfileIntent_fromSeedYaml() {
        val profile = PlatformProfiles.require("data-analysis")
        assertThat(profile.prompts.map { it.id }).contains("data-analysis.intent")
        val intent = profile.prompts.single { it.id == "data-analysis.intent" }
        assertTrue(intent.content.contains("sql-query.intent"))
        assertTrue(intent.content.contains("concept.intent"))
        assertTrue(intent.content.contains("metadata-authoring.intent"))
        assertTrue(intent.content.contains("agent instructions"))
        assertTrue(intent.content.contains("facet catalog"))
    }

    @Test
    fun shouldDocumentAiAnnotations_inSqlAndSchemaPrompts() {
        val schemaSystem = CapabilityManifest.load("capabilities/schema.yaml").promptAsset("schema.system")
        val sqlSystem = CapabilityManifest.load("capabilities/sql-query.yaml").promptAsset("sql-query.system")
        assertTrue(schemaSystem.content.contains("aiAnnotations"))
        assertTrue(sqlSystem.content.contains("aiAnnotations"))
    }

    @Test
    fun shouldComposeConceptIntent_inDataAnalysisProfile_withoutOwningDataQuery() {
        val intent = PlatformProfiles.require("data-analysis")
            .prompts.single { it.id == "data-analysis.intent" }
        assertTrue(intent.content.contains("concept.intent"))
        assertFalse(intent.content.contains("CONCEPT_LOOKUP"))
        val conceptIntent = CapabilityManifest.load("capabilities/concept.yaml")
            .promptAsset("concept.intent")
        assertFalse(conceptIntent.content.contains("DATA_QUERY"))
    }

    @Test
    fun shouldParseProfilePrompts_fromInlineYaml() {
        val registry = ResourceProfileRegistry.parse(
            """
            kind: AgentProfile
            id: mixed-profile
            capabilities:
              - conversation
            prompts:
              mixed-profile.intent:
                description: test
                content: |
                  Route mixed turns here.
            """.trimIndent(),
        )
        val profile = registry.resolve("mixed-profile")!!
        assertThat(profile.prompts).hasSize(1)
        assertThat(profile.prompts.single().id).isEqualTo("mixed-profile.intent")
    }

    @Test
    fun shouldEmitProfilePromptsBeforeCapabilityPrompts_inSystemPrompt() {
        val profile = PlatformProfiles.require("data-analysis")
        val catalog = object : SchemaCatalogPort {
            override fun listSchemas(): List<ListSchemasItem> = emptyList()
            override fun listTables(schemaName: String): List<ListTablesItem> = emptyList()
            override fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem> = emptyList()
            override fun listRelations(schemaName: String, tableName: String, direction: RelationDirection): List<ListRelationsItem> =
                emptyList()
        }
        val dialect = DialectRegistry.fromClasspathDefaults().requireDialect("calcite")
        val context = AgentContext(
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
        val capabilities = registry.capabilitiesFor(profile, context)
        val systemPrompt = buildAgentSystemPrompt(profile, capabilities)

        val conceptIdx = systemPrompt.indexOf("concept.system")
        val profileIdx = systemPrompt.indexOf("data-analysis.intent")
        val sqlIntentIdx = systemPrompt.indexOf("sql-query.intent")
        val metadataIntentIdx = systemPrompt.indexOf("metadata-authoring.intent")
        assertTrue(profileIdx >= 0)
        assertTrue(conceptIdx > profileIdx)
        assertTrue(sqlIntentIdx > profileIdx)
        assertTrue(metadataIntentIdx > profileIdx)
    }

    @Test
    fun shouldUseProcedureFirstMetadataAuthoringReasoning_withoutToolEnumeration() {
        val reasoning = CapabilityManifest.load("capabilities/metadata-authoring.yaml")
            .promptAsset("metadata-authoring.reasoning")
        assertFalse(reasoning.content.contains("list_schemas"))
        assertFalse(reasoning.content.contains("propose_facet_assignment"))
        assertTrue(reasoning.content.contains("Ground every target"))
    }
}
