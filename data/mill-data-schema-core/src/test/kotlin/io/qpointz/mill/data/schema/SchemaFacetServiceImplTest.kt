package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.core.RelationFacet
import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.LogicalDataType
import io.qpointz.mill.proto.Schema
import io.qpointz.mill.proto.Table
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class SchemaFacetServiceImplTest {

    @Mock
    private lateinit var schemaProvider: SchemaProvider

    @Mock
    private lateinit var metadataRepository: MetadataRepository

    private lateinit var service: SchemaFacetServiceImpl

    @BeforeEach
    fun setUp() {
        service = SchemaFacetServiceImpl(schemaProvider, metadataRepository)
    }

    // ---- physical schema preservation ----

    @Test
    fun `all physical schemas are present in result when no metadata exists`() {
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("schema_a", "schema_b"))
        whenever(schemaProvider.getSchema("schema_a")).thenReturn(emptySchema())
        whenever(schemaProvider.getSchema("schema_b")).thenReturn(emptySchema())
        whenever(metadataRepository.findAll()).thenReturn(emptyList())

        val result = service.getSchemas()

        assertEquals(2, result.schemas.size)
        assertEquals(setOf("schema_a", "schema_b"), result.schemas.map { it.schemaName }.toSet())
    }

    @Test
    fun `all physical tables are preserved when no metadata exists`() {
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("myschema"))
        whenever(schemaProvider.getSchema("myschema")).thenReturn(
            schema("myschema", table("myschema", "table_a"), table("myschema", "table_b"))
        )
        whenever(metadataRepository.findAll()).thenReturn(emptyList())

        val result = service.getSchemas()

        val tables = result.schemas.single().tables
        assertEquals(2, tables.size)
        assertEquals(setOf("table_a", "table_b"), tables.map { it.tableName }.toSet())
    }

    @Test
    fun `all physical columns are preserved when no metadata exists`() {
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(
            schema("s", table("s", "t", field("col_a"), field("col_b"), field("col_c")))
        )
        whenever(metadataRepository.findAll()).thenReturn(emptyList())

        val result = service.getSchemas()

        val columns = result.schemas.single().tables.single().columns
        assertEquals(3, columns.size)
        assertEquals(setOf("col_a", "col_b", "col_c"), columns.map { it.columnName }.toSet())
    }

    @Test
    fun `physical attribute properties are carried through`() {
        val dataType = DataType.newBuilder()
            .setNullability(DataType.Nullability.NOT_NULL)
            .setType(LogicalDataType.newBuilder().setTypeId(LogicalDataType.LogicalDataTypeId.STRING).build())
            .build()
        val f = Field.newBuilder().setName("name_col").setFieldIdx(2).setType(dataType).build()

        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t", f)))
        whenever(metadataRepository.findAll()).thenReturn(emptyList())

        val result = service.getSchemas()
        val attr = result.schemas.single().tables.single().columns.single()

        assertEquals("name_col", attr.columnName)
        assertEquals(2, attr.fieldIndex)
        assertEquals(DataType.Nullability.NOT_NULL, attr.dataType.nullability)
    }

    // ---- metadata matching ----

    @Test
    fun `schema-level metadata is matched by schema name`() {
        val entity = metadataEntity("schema_a", null, null, "schema_a")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("schema_a"))
        whenever(schemaProvider.getSchema("schema_a")).thenReturn(emptySchema())
        whenever(metadataRepository.findAll()).thenReturn(listOf(entity))

        val result = service.getSchemas()

        val schema = result.schemas.single()
        assertNotNull(schema.metadata)
        assertEquals("schema_a", schema.metadata!!.id)
        assertTrue(schema.hasMetadata)
    }

    @Test
    fun `table-level metadata is matched by schema and table name`() {
        val entity = metadataEntity("s", "t", null, "s.t")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t")))
        whenever(metadataRepository.findAll()).thenReturn(listOf(entity))

        val result = service.getSchemas()

        val tbl = result.schemas.single().tables.single()
        assertNotNull(tbl.metadata)
        assertEquals("s.t", tbl.metadata!!.id)
    }

    @Test
    fun `column-level metadata is matched by schema, table, and column name`() {
        val entity = metadataEntity("s", "t", "col", "s.t.col")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t", field("col"))))
        whenever(metadataRepository.findAll()).thenReturn(listOf(entity))

        val result = service.getSchemas()

        val attr = result.schemas.single().tables.single().columns.single()
        assertNotNull(attr.metadata)
        assertEquals("s.t.col", attr.metadata!!.id)
    }

    @Test
    fun `schema with no metadata has null metadata and empty facets`() {
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(emptySchema())
        whenever(metadataRepository.findAll()).thenReturn(emptyList())

        val schema = service.getSchemas().schemas.single()

        assertNull(schema.metadata)
        assertFalse(schema.hasMetadata)
        assertTrue(schema.facets.isEmpty)
    }

    @Test
    fun `table metadata does not match schema-only entity`() {
        val schemaOnlyEntity = metadataEntity("s", null, null, "s")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t")))
        whenever(metadataRepository.findAll()).thenReturn(listOf(schemaOnlyEntity))

        val tbl = service.getSchemas().schemas.single().tables.single()

        assertNull(tbl.metadata)
    }

    @Test
    fun `column metadata does not match table entity`() {
        val tableEntity = metadataEntity("s", "t", null, "s.t")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t", field("col"))))
        whenever(metadataRepository.findAll()).thenReturn(listOf(tableEntity))

        val attr = service.getSchemas().schemas.single().tables.single().columns.single()

        assertNull(attr.metadata)
    }

    // ---- unbound / stale metadata ----

    @Test
    fun `metadata entity referencing missing schema goes to unboundMetadata`() {
        val stale = metadataEntity("ghost_schema", null, null, "ghost_schema")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("real_schema"))
        whenever(schemaProvider.getSchema("real_schema")).thenReturn(emptySchema())
        whenever(metadataRepository.findAll()).thenReturn(listOf(stale))

        val result = service.getSchemas()

        assertEquals(1, result.unboundMetadata.size)
        assertEquals("ghost_schema", result.unboundMetadata.single().id)
    }

    @Test
    fun `metadata entity referencing missing table goes to unboundMetadata`() {
        val stale = metadataEntity("s", "ghost_table", null, "s.ghost_table")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "real_table")))
        whenever(metadataRepository.findAll()).thenReturn(listOf(stale))

        val result = service.getSchemas()

        assertEquals(1, result.unboundMetadata.size)
        assertEquals("s.ghost_table", result.unboundMetadata.single().id)
    }

    @Test
    fun `metadata entity referencing missing column goes to unboundMetadata`() {
        val stale = metadataEntity("s", "t", "ghost_col", "s.t.ghost_col")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t", field("real_col"))))
        whenever(metadataRepository.findAll()).thenReturn(listOf(stale))

        val result = service.getSchemas()

        assertEquals(1, result.unboundMetadata.size)
        assertEquals("s.t.ghost_col", result.unboundMetadata.single().id)
    }

    @Test
    fun `matched metadata is not duplicated in unboundMetadata`() {
        val matched = metadataEntity("s", "t", null, "s.t")
        val unmatched = metadataEntity("s", "ghost", null, "s.ghost")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t")))
        whenever(metadataRepository.findAll()).thenReturn(listOf(matched, unmatched))

        val result = service.getSchemas()

        assertEquals(1, result.unboundMetadata.size)
        assertEquals("s.ghost", result.unboundMetadata.single().id)
    }

    @Test
    fun `entity with null id is excluded from unbound tracking`() {
        val noId = MetadataEntity().apply {
            schemaName = "ghost"; tableName = null; attributeName = null; id = null
        }
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(emptySchema())
        whenever(metadataRepository.findAll()).thenReturn(listOf(noId))

        val result = service.getSchemas()

        assertTrue(result.unboundMetadata.isEmpty())
    }

    // ---- facet attachment ----

    @Test
    fun `descriptive facet is attached from matched table entity`() {
        val entity = metadataEntity("s", "t", null, "s.t").apply {
            setFacet("descriptive", MetadataUrns.SCOPE_GLOBAL, mapOf(
                "displayName" to "My Table",
                "description" to "A test table"
            ))
        }

        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t")))
        whenever(metadataRepository.findAll()).thenReturn(listOf(entity))

        val tbl = service.getSchemas().schemas.single().tables.single()

        assertNotNull(tbl.facets.descriptive)
        assertEquals("My Table", tbl.facets.descriptive!!.displayName)
    }

    @Test
    fun `facetByType returns correct facet for known type`() {
        val entity = metadataEntity("s", "t", null, "s.t").apply {
            setFacet("relation", MetadataUrns.SCOPE_GLOBAL, mapOf("relations" to emptyList<Any>()))
        }

        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t")))
        whenever(metadataRepository.findAll()).thenReturn(listOf(entity))

        val tbl = service.getSchemas().schemas.single().tables.single()

        assertNotNull(tbl.facets.facetByType<RelationFacet>("relation"))
    }

    @Test
    fun `absent facets resolve to null on typed properties`() {
        val entity = metadataEntity("s", "t", null, "s.t")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t")))
        whenever(metadataRepository.findAll()).thenReturn(listOf(entity))

        val tbl = service.getSchemas().schemas.single().tables.single()

        assertNull(tbl.facets.descriptive)
        assertNull(tbl.facets.relation)
        assertNull(tbl.facets.structural)
    }

    // ---- traceability ----

    @Test
    fun `SchemaWithFacets carries schema name for traceability`() {
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("tracing_schema"))
        whenever(schemaProvider.getSchema("tracing_schema")).thenReturn(emptySchema())
        whenever(metadataRepository.findAll()).thenReturn(emptyList())

        val schema = service.getSchemas().schemas.single()

        assertEquals("tracing_schema", schema.schemaName)
    }

    @Test
    fun `SchemaTableWithFacets carries schema and table name for traceability`() {
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "trace_table")))
        whenever(metadataRepository.findAll()).thenReturn(emptyList())

        val tbl = service.getSchemas().schemas.single().tables.single()

        assertEquals("s", tbl.schemaName)
        assertEquals("trace_table", tbl.tableName)
    }

    @Test
    fun `SchemaColumnWithFacets carries schema, table, and column name for traceability`() {
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t", field("trace_col"))))
        whenever(metadataRepository.findAll()).thenReturn(emptyList())

        val attr = service.getSchemas().schemas.single().tables.single().columns.single()

        assertEquals("s", attr.schemaName)
        assertEquals("t", attr.tableName)
        assertEquals("trace_col", attr.columnName)
    }

    @Test
    fun `matched metadata entity is accessible for traceability from WithFacets object`() {
        val entity = metadataEntity("s", "t", "col", "s.t.col")
        whenever(schemaProvider.getSchemaNames()).thenReturn(listOf("s"))
        whenever(schemaProvider.getSchema("s")).thenReturn(schema("s", table("s", "t", field("col"))))
        whenever(metadataRepository.findAll()).thenReturn(listOf(entity))

        val attr = service.getSchemas().schemas.single().tables.single().columns.single()

        assertNotNull(attr.metadata)
        assertEquals("s.t.col", attr.metadata!!.id)
    }

    // ---- helpers ----

    private fun emptySchema(): Schema = Schema.newBuilder().build()

    private fun schema(schemaName: String, vararg tables: Table): Schema =
        Schema.newBuilder().addAllTables(tables.toList()).build()

    private fun table(schemaName: String, name: String, vararg fields: Field): Table =
        Table.newBuilder()
            .setSchemaName(schemaName)
            .setName(name)
            .setTableType(Table.TableTypeId.TABLE)
            .addAllFields(fields.toList())
            .build()

    private fun field(name: String, idx: Int = 0): Field =
        Field.newBuilder()
            .setName(name)
            .setFieldIdx(idx)
            .setType(DataType.newBuilder().setNullability(DataType.Nullability.NOT_NULL).build())
            .build()

    private fun metadataEntity(
        schemaName: String?,
        tableName: String?,
        attributeName: String?,
        id: String
    ): MetadataEntity = MetadataEntity().apply {
        this.id = id
        this.schemaName = schemaName
        this.tableName = tableName
        this.attributeName = attributeName
        // No facets set — getFacet will naturally return Optional.empty()
    }
}
