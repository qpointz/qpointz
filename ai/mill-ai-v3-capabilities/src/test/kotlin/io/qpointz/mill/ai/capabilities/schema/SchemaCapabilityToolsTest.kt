package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.CapabilityDependencies
import io.qpointz.mill.ai.ToolDefinition
import io.qpointz.mill.ai.ToolInvocationSimulator
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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

/**
 * Tool-contract tests for [SchemaCapability].
 *
 * These tests drive each tool through the same JSON-in / JSON-out path the LLM runtime uses:
 * raw JSON argument string → [ToolRequest] → [argumentsAs] → handler → serialized result.
 *
 * They do NOT test handler domain logic (that is [SchemaToolHandlersTest]).
 * They DO test:
 * - tool is discoverable by name from [Capability.tools]
 * - JSON argument string is correctly deserialized into typed args
 * - enum fields coerce from string ("OUTBOUND" → [RelationDirection.OUTBOUND])
 * - optional fields with defaults work when absent from JSON
 * - missing required fields produce a clear error before reaching the handler
 * - result content serializes to valid JSON with expected field names
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchemaCapabilityToolsTest {

    @Mock
    private lateinit var schemaService: SchemaFacetService

    private lateinit var capability: Capability
    private val sim = ToolInvocationSimulator

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
                SchemaAttributeWithFacets("schema1", "tableA1", "col_bool", 0,
                    DataType.newBuilder()
                        .setNullability(DataType.Nullability.NULL)
                        .setType(LogicalDataType.newBuilder().setTypeId(LogicalDataType.LogicalDataTypeId.BOOL))
                        .build(),
                    null, SchemaFacets(setOf(DescriptiveFacet(description = "A boolean column")))),
                SchemaAttributeWithFacets("schema1", "tableA1", "col_bigint", 1,
                    DataType.newBuilder()
                        .setNullability(DataType.Nullability.NOT_NULL)
                        .setType(LogicalDataType.newBuilder().setTypeId(LogicalDataType.LogicalDataTypeId.BIG_INT))
                        .build(),
                    null, SchemaFacets(emptySet()))
            ), null, SchemaFacets(setOf(
                DescriptiveFacet(description = "tableA1 description"),
                RelationFacet(mutableListOf(outboundRelation, inboundRelation, outboundRelation2))
            ))),
            SchemaTableWithFacets("schema1", "tableA2", Table.TableTypeId.TABLE, emptyList(),
                null, SchemaFacets(emptySet())),
            SchemaTableWithFacets("schema1", "tableA3", Table.TableTypeId.TABLE, emptyList(),
                null, SchemaFacets(emptySet())),
        ), null, SchemaFacets(setOf(DescriptiveFacet(description = "schema1 description"))))

        val schema2 = SchemaWithFacets("schema2", emptyList(), null, SchemaFacets(emptySet()))

        whenever(schemaService.getSchemas())
            .thenReturn(SchemaFacetResult(listOf(schema1, schema2), emptyList()))

        capability = SchemaCapabilityProvider().create(
            AgentContext(contextType = "general"),
            CapabilityDependencies.of(SchemaCapabilityDependency(schemaService))
        )
    }

    // ── tool discovery ────────────────────────────────────────────────────────

    @Test
    fun `capability exposes exactly four schema tools`() {
        assertEquals(
            setOf("list_schemas", "list_tables", "list_columns", "list_relations"),
            capability.tools.map { it.name }.toSet()
        )
    }

    // ── list_schemas ──────────────────────────────────────────────────────────

    @Test
    fun `list_schemas accepts empty arguments`() {
        val rows = sim.parseList(sim.invoke(tool("list_schemas"), ""))
        assertFalse(rows.isEmpty())
    }

    @Test
    fun `list_schemas result contains schemaName field`() {
        val rows = sim.parseList(sim.invoke(tool("list_schemas")))
        assertTrue(rows.all { it.containsKey("schemaName") })
    }

    @Test
    fun `list_schemas result contains description field`() {
        val rows = sim.parseList(sim.invoke(tool("list_schemas")))
        assertTrue(rows.all { it.containsKey("description") })
    }

    @Test
    fun `list_schemas returns all schemas`() {
        val names = sim.parseList(sim.invoke(tool("list_schemas"))).map { it["schemaName"] }
        assertTrue(names.contains("schema1"))
        assertTrue(names.contains("schema2"))
    }

    @Test
    fun `list_schemas carries schema description`() {
        val rows = sim.parseList(sim.invoke(tool("list_schemas")))
        val schema1 = rows.single { it["schemaName"] == "schema1" }
        assertEquals("schema1 description", schema1["description"])
    }

    @Test
    fun `list_schemas returns empty description for schema without metadata`() {
        val rows = sim.parseList(sim.invoke(tool("list_schemas")))
        val schema2 = rows.single { it["schemaName"] == "schema2" }
        assertEquals("", schema2["description"])
    }

    // ── list_tables ───────────────────────────────────────────────────────────

    @Test
    fun `list_tables returns tables for known schema`() {
        val rows = sim.parseList(sim.invoke(tool("list_tables"), """{"schemaName":"schema1"}"""))
        assertFalse(rows.isEmpty())
    }

    @Test
    fun `list_tables result contains tableName field`() {
        val rows = sim.parseList(sim.invoke(tool("list_tables"), """{"schemaName":"schema1"}"""))
        assertTrue(rows.all { it.containsKey("tableName") })
    }

    @Test
    fun `list_tables result contains schemaName field`() {
        val rows = sim.parseList(sim.invoke(tool("list_tables"), """{"schemaName":"schema1"}"""))
        assertTrue(rows.all { it["schemaName"] == "schema1" })
    }

    @Test
    fun `list_tables carries table description`() {
        val rows = sim.parseList(sim.invoke(tool("list_tables"), """{"schemaName":"schema1"}"""))
        val tableA1 = rows.single { it["tableName"] == "tableA1" }
        assertEquals("tableA1 description", tableA1["description"])
    }

    @Test
    fun `list_tables returns empty list for unknown schema`() {
        val rows = sim.parseList(sim.invoke(tool("list_tables"), """{"schemaName":"does_not_exist"}"""))
        assertTrue(rows.isEmpty())
    }

    @Test
    fun `list_tables throws when schemaName is missing`() {
        assertThrows<IllegalArgumentException> {
            sim.invoke(tool("list_tables"), "{}")
        }
    }

    // ── list_columns ──────────────────────────────────────────────────────────

    @Test
    fun `list_columns returns columns for known table`() {
        val rows = sim.parseList(sim.invoke(tool("list_columns"),
            """{"schemaName":"schema1","tableName":"tableA1"}"""))
        assertFalse(rows.isEmpty())
    }

    @Test
    fun `list_columns result contains expected fields`() {
        val rows = sim.parseList(sim.invoke(tool("list_columns"),
            """{"schemaName":"schema1","tableName":"tableA1"}"""))
        val col = rows.first()
        assertTrue(col.containsKey("columnName"))
        assertTrue(col.containsKey("type"))
        assertTrue(col.containsKey("nullable"))
        assertTrue(col.containsKey("description"))
    }

    @Test
    fun `list_columns maps column names correctly`() {
        val rows = sim.parseList(sim.invoke(tool("list_columns"),
            """{"schemaName":"schema1","tableName":"tableA1"}"""))
        val names = rows.map { it["columnName"] }
        assertTrue(names.contains("col_bool"))
        assertTrue(names.contains("col_bigint"))
    }

    @Test
    fun `list_columns serializes type as string`() {
        val rows = sim.parseList(sim.invoke(tool("list_columns"),
            """{"schemaName":"schema1","tableName":"tableA1"}"""))
        val col = rows.single { it["columnName"] == "col_bool" }
        assertEquals("BOOL", col["type"])
    }

    @Test
    fun `list_columns serializes nullable as string`() {
        val rows = sim.parseList(sim.invoke(tool("list_columns"),
            """{"schemaName":"schema1","tableName":"tableA1"}"""))
        val col = rows.single { it["columnName"] == "col_bool" }
        assertEquals("NULL", col["nullable"])
    }

    @Test
    fun `list_columns carries column description`() {
        val rows = sim.parseList(sim.invoke(tool("list_columns"),
            """{"schemaName":"schema1","tableName":"tableA1"}"""))
        val col = rows.single { it["columnName"] == "col_bool" }
        assertEquals("A boolean column", col["description"])
    }

    @Test
    fun `list_columns returns empty list for table with no columns`() {
        val rows = sim.parseList(sim.invoke(tool("list_columns"),
            """{"schemaName":"schema1","tableName":"tableA2"}"""))
        assertTrue(rows.isEmpty())
    }

    @Test
    fun `list_columns throws when tableName is missing`() {
        assertThrows<IllegalArgumentException> {
            sim.invoke(tool("list_columns"), """{"schemaName":"schema1"}""")
        }
    }

    // ── list_relations — direction coercion ───────────────────────────────────

    @Test
    fun `list_relations defaults direction to BOTH when absent`() {
        val rows = sim.parseList(sim.invoke(tool("list_relations"),
            """{"schemaName":"schema1","tableName":"tableA1"}"""))
        val names = rows.map { it["name"] }.toSet()
        assertEquals(setOf("order_customer", "order_product", "customer_order"), names)
    }

    @Test
    fun `list_relations accepts OUTBOUND string and filters to source relations`() {
        val rows = sim.parseList(sim.invoke(tool("list_relations"),
            """{"schemaName":"schema1","tableName":"tableA1","direction":"OUTBOUND"}"""))
        val names = rows.map { it["name"] }.toSet()
        assertEquals(setOf("order_customer", "order_product"), names)
    }

    @Test
    fun `list_relations accepts INBOUND string and filters to target relations`() {
        val rows = sim.parseList(sim.invoke(tool("list_relations"),
            """{"schemaName":"schema1","tableName":"tableA1","direction":"INBOUND"}"""))
        val names = rows.map { it["name"] }.toSet()
        assertEquals(setOf("customer_order"), names)
    }

    @Test
    fun `list_relations accepts BOTH string and returns all relations`() {
        val rows = sim.parseList(sim.invoke(tool("list_relations"),
            """{"schemaName":"schema1","tableName":"tableA1","direction":"BOTH"}"""))
        val names = rows.map { it["name"] }.toSet()
        assertEquals(setOf("order_customer", "order_product", "customer_order"), names)
    }

    @Test
    fun `list_relations result contains expected fields`() {
        val rows = sim.parseList(sim.invoke(tool("list_relations"),
            """{"schemaName":"schema1","tableName":"tableA1","direction":"OUTBOUND"}"""))
        val row = rows.first()
        assertTrue(row.containsKey("sourceSchema"))
        assertTrue(row.containsKey("sourceTable"))
        assertTrue(row.containsKey("sourceAttributes"))
        assertTrue(row.containsKey("targetSchema"))
        assertTrue(row.containsKey("targetTable"))
        assertTrue(row.containsKey("targetAttributes"))
        assertTrue(row.containsKey("name"))
        assertTrue(row.containsKey("description"))
        assertTrue(row.containsKey("cardinality"))
    }

    @Test
    fun `list_relations serializes sourceAttributes as JSON array`() {
        val rows = sim.parseList(sim.invoke(tool("list_relations"),
            """{"schemaName":"schema1","tableName":"tableA1","direction":"OUTBOUND"}"""))
        val row = rows.single { it["name"] == "order_customer" }
        @Suppress("UNCHECKED_CAST")
        val sourceAttrs = row["sourceAttributes"] as List<String>
        assertEquals(listOf("customer_id"), sourceAttrs)
    }

    @Test
    fun `list_relations serializes cardinality as string`() {
        val rows = sim.parseList(sim.invoke(tool("list_relations"),
            """{"schemaName":"schema1","tableName":"tableA1","direction":"OUTBOUND"}"""))
        val row = rows.single { it["name"] == "order_customer" }
        assertEquals("MANY_TO_ONE", row["cardinality"])
    }

    @Test
    fun `list_relations throws when schemaName is missing`() {
        assertThrows<IllegalArgumentException> {
            sim.invoke(tool("list_relations"), """{"tableName":"tableA1"}""")
        }
    }

    @Test
    fun `list_relations throws when tableName is missing`() {
        assertThrows<IllegalArgumentException> {
            sim.invoke(tool("list_relations"), """{"schemaName":"schema1"}""")
        }
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private fun tool(name: String): ToolDefinition =
        capability.tools.single { it.name == name }
}
