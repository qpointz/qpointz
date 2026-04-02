package io.qpointz.mill.data.backend.flow

import io.qpointz.mill.data.metadata.FlowInferredFacetTypeKeys
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.source.MetadataOriginIds
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Path

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [FlowFacetMetadataTestApplication::class])
class FlowDescriptorMetadataSourceIT {

    @Autowired
    private lateinit var schemaFacetService: SchemaFacetService

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun flowMetadataProperties(registry: DynamicPropertyRegistry) {
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
    fun `merged schema facets include flow origin and flow-schema URn`() {
        val schema =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
        val flowRows =
            schema.facets.facetsResolved.filter { it.originId == MetadataOriginIds.FLOW }
        val expectedType = MetadataEntityUrn.canonicalize(FlowInferredFacetTypeKeys.SCHEMA)
        assertTrue(
            flowRows.any { MetadataEntityUrn.canonicalize(it.facetTypeKey) == expectedType },
            { "Expected a flow-schema facet in ${flowRows.map { it.facetTypeKey }}" },
        )
    }

    @Test
    fun `merged table facets include flow-table for cities`() {
        val table =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
                .tables.single { it.tableName.equals("cities", ignoreCase = true) }
        val flowRows = table.facets.facetsResolved.filter { it.originId == MetadataOriginIds.FLOW }
        val expectedType = MetadataEntityUrn.canonicalize(FlowInferredFacetTypeKeys.TABLE)
        assertTrue(
            flowRows.any { MetadataEntityUrn.canonicalize(it.facetTypeKey) == expectedType },
            { "Expected a flow-table facet in ${flowRows.map { it.facetTypeKey }}" },
        )
    }

    @Test
    fun `merged column facets include flow-column for cities id`() {
        val column =
            schemaFacetService.getSchemas().schemas
                .single { it.schemaName.equals("skymill", ignoreCase = true) }
                .tables.single { it.tableName.equals("cities", ignoreCase = true) }
                .columns.single { it.columnName.equals("id", ignoreCase = true) }
        val flowRows = column.facets.facetsResolved.filter { it.originId == MetadataOriginIds.FLOW }
        val expectedType = MetadataEntityUrn.canonicalize(FlowInferredFacetTypeKeys.COLUMN)
        assertTrue(
            flowRows.any { MetadataEntityUrn.canonicalize(it.facetTypeKey) == expectedType },
            { "Expected a flow-column facet in ${flowRows.map { it.facetTypeKey }}" },
        )
    }
}
