package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.data.schema.*
import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.metadata.domain.RelationType
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.metadata.domain.core.EntityReference
import io.qpointz.mill.metadata.domain.core.RelationFacet
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType
import io.qpointz.mill.proto.Table
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class SchemaToolHandlersTest {

    @Mock
    private lateinit var schemaService: SchemaFacetService

    @BeforeEach
    fun setUp() {
        val outboundRelation = RelationFacet.Relation(
            name = "order_customer",
            description = "Orders belong to a customer",
            sourceTable = EntityReference(schema = "schema1", table = "tableA1"),
            sourceAttributes = listOf("customer_id"),
            targetTable = EntityReference(schema = "schema1", table = "tableA2"),
            targetAttributes = listOf("id"),
            cardinality = RelationCardinality.MANY_TO_ONE,
            type = RelationType.FOREIGN_KEY,
            businessMeaning = "Each order is placed by one customer"
        )
        val inboundRelation = RelationFacet.Relation(
            name = "customer_order",
            description = "Customer has many orders",
            sourceTable = EntityReference(schema = "schema1", table = "tableA2"),
            sourceAttributes = listOf("id"),
            targetTable = EntityReference(schema = "schema1", table = "tableA1"),
            targetAttributes = listOf("customer_id"),
            cardinality = RelationCardinality.ONE_TO_MANY,
            type = RelationType.LOGICAL,
            businessMeaning = "A customer can have multiple orders"
        )
        val outboundRelation2 = RelationFacet.Relation(
            name = "order_product",
            description = "Orders reference a product",
            sourceTable = EntityReference(schema = "schema1", table = "tableA1"),
            sourceAttributes = listOf("product_id"),
            targetTable = EntityReference(schema = "schema1", table = "tableA3"),
            targetAttributes = listOf("id"),
            cardinality = RelationCardinality.MANY_TO_ONE,
            type = RelationType.FOREIGN_KEY,
            businessMeaning = "Each order references one product"
        )

        val schema1 = SchemaWithFacets("schema1", listOf(
            SchemaTableWithFacets("schema1", "tableA1", Table.TableTypeId.TABLE, listOf(
                SchemaAttributeWithFacets("schema1", "tableA1", "attribute1", 0,
                        DataType.newBuilder().setNullability(DataType.Nullability.NULL).setType(LogicalDataType.newBuilder().setTypeId(
                            LogicalDataType.LogicalDataTypeId.BOOL)).build(), null, SchemaFacets(setOf(DescriptiveFacet(
                        description = "attribute 1"
                    )))),
                SchemaAttributeWithFacets("schema1", "tableA1", "attribute2", 1,
                    DataType.newBuilder().setNullability(DataType.Nullability.NOT_NULL).setType(LogicalDataType.newBuilder().setTypeId(
                        LogicalDataType.LogicalDataTypeId.BIG_INT)).build(), null, SchemaFacets(setOf()))
            ), null, SchemaFacets(setOf(
                DescriptiveFacet(description = "table1description"),
                RelationFacet(mutableListOf(outboundRelation, inboundRelation, outboundRelation2))
            ))),
            SchemaTableWithFacets("schema1", "tableA2", Table.TableTypeId.TABLE, emptyList(), null, SchemaFacets(emptySet())),
            SchemaTableWithFacets("schema1", "tableA3", Table.TableTypeId.TABLE, emptyList(), null, SchemaFacets(emptySet())),
        ), null, SchemaFacets(setOf(
            DescriptiveFacet(description = "schema1")
        )))

        val schema2 = SchemaWithFacets("schema2", emptyList(), null, SchemaFacets(emptySet()))

        val facetSchemas = listOf(
            schema1, schema2
        )

        whenever(schemaService.getSchemas()).thenReturn(SchemaFacetResult(facetSchemas, listOf()))
    }

    @Test
    fun `returns_schema_list_with_description`()  {
        val schemas = SchemaToolHandlers.listSchemas(schemaService)
        assertNotNull(schemas)
        assertFalse(schemas.isEmpty())
        assertEquals("schema1", schemas.get(0).description)
        assertEquals("", schemas.get(1).description)
    }

    @Test
    fun `returns_table_list`() {
        val tables = SchemaToolHandlers.listTables(schemaService, "schema1")
        assertNotNull(tables)
        assertFalse(tables.isEmpty())
        assertEquals("table1description", tables.get(0).description)
        assertEquals("", tables.get(1).description)
    }

    @Test
    fun `returns_columns_list`() {
        val columns = SchemaToolHandlers.listColumns(schemaService, "schema1", "tableA1")
        assertNotNull(columns)
        assertFalse(columns.isEmpty())
        val col = columns[0]
        assertTrue { col.schemaName == "schema1" &&  col.tableName == "tableA1" && col.columnName== "attribute1" }
        assertTrue { col.type == LogicalDataType.LogicalDataTypeId.BOOL }
        assertTrue { col.nullable == DataType.Nullability.NULL }
    }

    @Test
    fun `returns_empty_columns_list`() {
        val columns = SchemaToolHandlers.listColumns(schemaService, "schema2", "tableA2")
        assertNotNull(columns)
        assertTrue(columns.isEmpty())
    }

    @Test
    fun `listRelations OUTBOUND returns only relations where tableA1 is source`() {
        val relations = SchemaToolHandlers.listRelations(schemaService, "schema1", "tableA1", SchemaToolHandlers.RelationDirection.OUTBOUND)
        assertEquals(setOf("order_customer", "order_product"), relations.map { it.name }.toSet())
    }

    @Test
    fun `listRelations INBOUND returns only relations where tableA1 is target`() {
        val relations = SchemaToolHandlers.listRelations(schemaService, "schema1", "tableA1", SchemaToolHandlers.RelationDirection.INBOUND)
        assertEquals(setOf("customer_order"), relations.map { it.name }.toSet())
    }

    @Test
    fun `listRelations BOTH returns all relations touching tableA1`() {
        val relations = SchemaToolHandlers.listRelations(schemaService, "schema1", "tableA1", SchemaToolHandlers.RelationDirection.BOTH)
        assertEquals(setOf("order_customer", "order_product", "customer_order"), relations.map { it.name }.toSet())
    }

    @Test
    fun `listRelations returns empty for table with no relation facet`() {
        val relations = SchemaToolHandlers.listRelations(schemaService, "schema1", "tableA1", SchemaToolHandlers.RelationDirection.BOTH)
        assertFalse(relations.isEmpty())
    }

    @Test
    fun `listRelations item maps relation fields correctly`() {
        val relations = SchemaToolHandlers.listRelations(schemaService, "schema1", "tableA1", SchemaToolHandlers.RelationDirection.OUTBOUND)
        val item = relations.first { it.name == "order_customer" }
        assertEquals("schema1", item.sourceSchema)
        assertEquals("tableA1", item.sourceTable)
        assertEquals(listOf("customer_id"), item.sourceAttributes)
        assertEquals("schema1", item.targetSchema)
        assertEquals("tableA2", item.targetTable)
        assertEquals(listOf("id"), item.targetAttributes)
        assertEquals(RelationCardinality.MANY_TO_ONE, item.cardinality)
        assertEquals("Orders belong to a customer", item.description)
    }

}