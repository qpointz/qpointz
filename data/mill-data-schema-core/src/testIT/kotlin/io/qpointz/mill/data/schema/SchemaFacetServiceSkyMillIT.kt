package io.qpointz.mill.data.schema

import io.qpointz.mill.data.schema.SkyMillSchemaProvider.Companion.SCHEMA_NAME
import io.qpointz.mill.data.schema.SkyMillSchemaProvider.Companion.TABLE_NO_METADATA
import io.qpointz.mill.metadata.configuration.MetadataCoreConfiguration
import io.qpointz.mill.metadata.configuration.MetadataRepositoryAutoConfiguration
import io.qpointz.mill.metadata.repository.MetadataRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [
    MetadataCoreConfiguration::class,
    MetadataRepositoryAutoConfiguration::class
])
class SchemaFacetServiceSkyMillIT {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun skymillProperties(registry: DynamicPropertyRegistry) {
            val dir = System.getProperty("skymill.datasets.dir")
                ?: error("System property 'skymill.datasets.dir' not set")
            registry.add("mill.metadata.file.path") { "file:$dir/skymill-meta-repository.yaml" }
            registry.add("mill.metadata.storage.type") { "file" }
        }
    }

    @Autowired
    private lateinit var metadataRepository: MetadataRepository

    private lateinit var service: SchemaFacetServiceImpl

    @BeforeEach
    fun setUp() {
        service = SchemaFacetServiceImpl(SkyMillSchemaProvider(), metadataRepository)
    }

    @Test
    fun `skymill schema is present in result`() {
        val result = service.getSchemas()

        assertTrue(result.schemas.any { it.schemaName == SCHEMA_NAME })
    }

    @Test
    fun `skymill schema carries descriptive facet from metadata`() {
        val schema = service.getSchemas().schemas.single { it.schemaName == SCHEMA_NAME }

        assertNotNull(schema.facets.descriptive)
        assertFalse(schema.facets.descriptive!!.description.isNullOrEmpty())
    }

    @Test
    fun `cities table is present in result`() {
        val schema = service.getSchemas().schemas.single { it.schemaName == SCHEMA_NAME }

        assertTrue(schema.tables.any { it.tableName == "cities" })
    }

    @Test
    fun `cities table has descriptive facet attached from metadata`() {
        val cities = service.getSchemas().schemas
            .single { it.schemaName == SCHEMA_NAME }
            .tables.single { it.tableName == "cities" }

        assertNotNull(cities.facets.descriptive)
        assertFalse(cities.facets.descriptive!!.description.isNullOrEmpty())
    }

    @Test
    fun `cities table has relation facet attached from metadata`() {
        val cities = service.getSchemas().schemas
            .single { it.schemaName == SCHEMA_NAME }
            .tables.single { it.tableName == "cities" }

        assertNotNull(cities.facets.relation)
    }

    @Test
    fun `all cities columns are present after merge`() {
        val cities = service.getSchemas().schemas
            .single { it.schemaName == SCHEMA_NAME }
            .tables.single { it.tableName == "cities" }

        val columnNames = cities.columns.map { it.columnName }.toSet()
        assertTrue(columnNames.containsAll(setOf("id", "city", "state", "airport_iata")))
    }

    @Test
    fun `cities column with metadata carries descriptive facet`() {
        val cityAttr = service.getSchemas().schemas
            .single { it.schemaName == SCHEMA_NAME }
            .tables.single { it.tableName == "cities" }
            .columns.single { it.columnName == "city" }

        assertNotNull(cityAttr.facets.descriptive)
    }

    @Test
    fun `table with no metadata entry is still present in result`() {
        val schema = service.getSchemas().schemas.single { it.schemaName == SCHEMA_NAME }
        val noMetaTable = schema.tables.single { it.tableName == TABLE_NO_METADATA }

        assertNotNull(noMetaTable)
        assertFalse(noMetaTable.hasMetadata)
        assertNull(noMetaTable.metadata)
        assertTrue(noMetaTable.facets.isEmpty)
    }

    @Test
    fun `segments table is present and preserves all physical columns`() {
        val segments = service.getSchemas().schemas
            .single { it.schemaName == SCHEMA_NAME }
            .tables.single { it.tableName == "segments" }

        val columnNames = segments.columns.map { it.columnName }.toSet()
        assertTrue(columnNames.containsAll(setOf("id", "origin", "destination", "distance")))
    }

    @Test
    fun `unbound metadata only contains entities not matched to physical schema`() {
        val result = service.getSchemas()

        // All unbound entries should reference coordinates absent from the physical schema
        for (entity in result.unboundMetadata) {
            val schemaMatch = result.schemas.none { it.schemaName == entity.schemaName }
                    || result.schemas.any { s ->
                s.schemaName == entity.schemaName && entity.tableName != null &&
                        s.tables.none { t ->
                            t.tableName == entity.tableName &&
                                    (entity.attributeName == null ||
                                            t.columns.any { c -> c.columnName == entity.attributeName })
                        }
            }
            assertTrue(schemaMatch, "Entity ${entity.id} should be unbound but matched a physical entity")
        }
    }
}
