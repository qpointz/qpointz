package io.qpointz.mill.data.backend.flow

import io.qpointz.mill.data.metadata.FlowInferredFacetTypeKeys
import io.qpointz.mill.data.metadata.LayoutInferredFacetTypeKeys
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.source.MetadataOriginIds
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Path

/**
 * Integration test validating metadata facet inference with **all metadata enabled** (default).
 * Both [FlowDescriptorMetadataSource] and [io.qpointz.mill.data.metadata.source.LogicalLayoutMetadataSource]
 * contribute facets for skymill schema entities.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [FlowFacetMetadataTestApplication::class])
class FlowMetadataAllEnabledIT {

    @Autowired
    private lateinit var schemaFacetService: SchemaFacetService

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            val root =
                System.getProperty("flow.facet.it.root")
                    ?: error("System property 'flow.facet.it.root' not set")
            val yamlPath =
                Path.of(root, "data", "mill-data-backends", "config", "test", "flow-skymill.yaml")
            registry.add("mill.data.backend.type") { "flow" }
            registry.add("mill.data.backend.flow.sources[0]") { yamlPath.toString() }
            registry.add("mill.metadata.repository.type") { "file" }
            registry.add("mill.metadata.seed.resources[0]") { "classpath:metadata/platform-bootstrap.yaml" }
            registry.add("mill.metadata.seed.resources[1]") { "classpath:metadata/platform-flow-facet-types.yaml" }
        }
    }

    @Test
    fun `schema facets include both flow and logical-layout origins`() {
        val schema =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
        val origins = schema.facets.facetsResolved.map { it.originId }.toSet()
        assertThat(origins)
            .contains(MetadataOriginIds.FLOW, MetadataOriginIds.LOGICAL_LAYOUT)
    }

    @Test
    fun `table facets include flow-table origin for cities`() {
        val table =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
                .tables.single { it.tableName.equals("cities", ignoreCase = true) }
        val flowFacets = table.facets.facetsResolved
            .filter { it.originId == MetadataOriginIds.FLOW }
        val expectedType = MetadataEntityUrn.canonicalize(FlowInferredFacetTypeKeys.TABLE)
        assertThat(flowFacets).anyMatch {
            MetadataEntityUrn.canonicalize(it.facetTypeKey) == expectedType
        }
    }

    @Test
    fun `table facets include logical-layout origin for cities`() {
        val table =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
                .tables.single { it.tableName.equals("cities", ignoreCase = true) }
        val layoutFacets = table.facets.facetsResolved
            .filter { it.originId == MetadataOriginIds.LOGICAL_LAYOUT }
        val expectedType = MetadataEntityUrn.canonicalize(LayoutInferredFacetTypeKeys.TABLE)
        assertThat(layoutFacets).anyMatch {
            MetadataEntityUrn.canonicalize(it.facetTypeKey) == expectedType
        }
    }

    @Test
    fun `column facets include both origins for cities id`() {
        val column =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
                .tables.single { it.tableName.equals("cities", ignoreCase = true) }
                .columns.single { it.columnName.equals("id", ignoreCase = true) }
        val origins = column.facets.facetsResolved.map { it.originId }.toSet()
        assertThat(origins)
            .contains(MetadataOriginIds.FLOW, MetadataOriginIds.LOGICAL_LAYOUT)
    }

    @Test
    fun `flow schema facet payload contains storage info`() {
        val schema =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
        val flowSchemaFacet = schema.facets.facetsResolved
            .filter { it.originId == MetadataOriginIds.FLOW }
            .single {
                MetadataEntityUrn.canonicalize(it.facetTypeKey) ==
                    MetadataEntityUrn.canonicalize(FlowInferredFacetTypeKeys.SCHEMA)
            }
        @Suppress("UNCHECKED_CAST")
        val storage = flowSchemaFacet.payload["storage"] as? Map<String, Any?>
        assertThat(storage).isNotNull
        assertThat(storage!!["type"]).isEqualTo("local")
    }
}

/**
 * Integration test validating that **global metadata disabled** suppresses all inferred facets.
 * When {@code mill.data.backend.metadata.enabled=false}, neither flow nor logical-layout facets
 * should appear.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [FlowFacetMetadataTestApplication::class])
class FlowMetadataGlobalDisabledIT {

    @Autowired
    private lateinit var schemaFacetService: SchemaFacetService

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            val root =
                System.getProperty("flow.facet.it.root")
                    ?: error("System property 'flow.facet.it.root' not set")
            val yamlPath =
                Path.of(root, "data", "mill-data-backends", "config", "test", "flow-skymill.yaml")
            registry.add("mill.data.backend.type") { "flow" }
            registry.add("mill.data.backend.flow.sources[0]") { yamlPath.toString() }
            registry.add("mill.data.backend.metadata.enabled") { "false" }
            registry.add("mill.metadata.repository.type") { "file" }
            registry.add("mill.metadata.seed.resources[0]") { "classpath:metadata/platform-bootstrap.yaml" }
            registry.add("mill.metadata.seed.resources[1]") { "classpath:metadata/platform-flow-facet-types.yaml" }
        }
    }

    @Test
    fun `schema facets contain no flow origin when global metadata disabled`() {
        val schema =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
        val flowFacets = schema.facets.facetsResolved
            .filter { it.originId == MetadataOriginIds.FLOW }
        assertThat(flowFacets).isEmpty()
    }

    @Test
    fun `schema facets contain no logical-layout origin when global metadata disabled`() {
        val schema =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
        val layoutFacets = schema.facets.facetsResolved
            .filter { it.originId == MetadataOriginIds.LOGICAL_LAYOUT }
        assertThat(layoutFacets).isEmpty()
    }

    @Test
    fun `table facets contain no inferred origins when global metadata disabled`() {
        val table =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
                .tables.single { it.tableName.equals("cities", ignoreCase = true) }
        val inferredOrigins = setOf(MetadataOriginIds.FLOW, MetadataOriginIds.LOGICAL_LAYOUT)
        val inferredFacets = table.facets.facetsResolved
            .filter { it.originId in inferredOrigins }
        assertThat(inferredFacets).isEmpty()
    }
}
