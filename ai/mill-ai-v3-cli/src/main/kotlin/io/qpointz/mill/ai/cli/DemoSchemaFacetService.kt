package io.qpointz.mill.ai.cli

import io.qpointz.mill.data.metadata.SchemaModelRoot
import io.qpointz.mill.data.schema.*
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.metadata.domain.RelationType
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.core.TableLocator
import io.qpointz.mill.data.schema.facet.RelationFacet
import io.qpointz.mill.metadata.service.MetadataContext
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType
import io.qpointz.mill.proto.Table

/**
 * In-memory demo retail schema for CLI testing without a live data source.
 *
 * Schema: retail
 *   - customers      — registered customers
 *   - products       — product catalogue
 *   - orders         — customer purchase orders
 *   - order_items    — individual line items within an order
 *   - raw_events     — no description (tests the missing-metadata case)
 */
internal class DemoSchemaFacetService : SchemaFacetService {

    private fun demoModelRoot(): ModelRootWithFacets = ModelRootWithFacets(
        metadataEntityId = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID),
        metadata = null,
        facets = SchemaFacets.EMPTY
    )

    override fun getModelRoot(context: MetadataContext): ModelRootWithFacets = demoModelRoot()

    override fun getSchemas(context: MetadataContext): SchemaFacetResult =
        SchemaFacetResult(
            modelRoot = demoModelRoot(),
            schemas = listOf(retailSchema()),
            unboundMetadata = emptyList()
        )

    override fun getSchema(schemaName: String, context: MetadataContext): SchemaWithFacets? =
        retailSchema().takeIf { it.schemaName == schemaName }

    override fun getTable(
        schemaName: String,
        tableName: String,
        context: MetadataContext
    ): SchemaTableWithFacets? =
        getSchema(schemaName, context)?.tables?.firstOrNull { it.tableName == tableName }

    override fun getColumn(
        schemaName: String,
        tableName: String,
        columnName: String,
        context: MetadataContext
    ): SchemaColumnWithFacets? =
        getTable(schemaName, tableName, context)?.columns?.firstOrNull { it.columnName == columnName }

    // ── Schema ────────────────────────────────────────────────────────────────

    private fun retailSchema(): SchemaWithFacets {
        val custRef  = TableLocator(schema = "retail", table = "customers")
        val ordRef   = TableLocator(schema = "retail", table = "orders")
        val itemRef  = TableLocator(schema = "retail", table = "order_items")
        val prodRef  = TableLocator(schema = "retail", table = "products")

        val custToOrders  = rel("customers_orders",  "A customer can have many orders",       custRef, listOf("id"),          ordRef,  listOf("customer_id"), RelationCardinality.ONE_TO_MANY,  RelationType.LOGICAL,      "Each customer places zero or more orders")
        val ordToCustomer = rel("orders_customer",   "Each order belongs to a customer",      ordRef,  listOf("customer_id"), custRef, listOf("id"),          RelationCardinality.MANY_TO_ONE, RelationType.FOREIGN_KEY,  "The customer who placed this order")
        val ordToItems    = rel("orders_items",      "An order contains one or more items",   ordRef,  listOf("id"),          itemRef, listOf("order_id"),    RelationCardinality.ONE_TO_MANY,  RelationType.LOGICAL,      "Line items that belong to this order")
        val itemToOrder   = rel("items_order",       "Each line item belongs to one order",   itemRef, listOf("order_id"),    ordRef,  listOf("id"),          RelationCardinality.MANY_TO_ONE, RelationType.FOREIGN_KEY,  "The order this line item belongs to")
        val itemToProduct = rel("items_product",     "Each line item references a product",   itemRef, listOf("product_id"), prodRef, listOf("id"),          RelationCardinality.MANY_TO_ONE, RelationType.FOREIGN_KEY,  "The product this line item is for")
        val prodToItems   = rel("products_items",    "A product may appear on many lines",    prodRef, listOf("id"),         itemRef, listOf("product_id"),  RelationCardinality.ONE_TO_MANY,  RelationType.LOGICAL,      "All order lines that reference this product")

        return SchemaWithFacets(
            schemaName = "retail",
            tables = listOf(
                customersTable(listOf(custToOrders)),
                productsTable(listOf(prodToItems)),
                ordersTable(listOf(ordToCustomer, ordToItems)),
                orderItemsTable(listOf(itemToOrder, itemToProduct)),
                rawEventsTable(),
            ),
            metadata = null,
            facets = descFacets("Demo retail schema with customers, orders, products and line items"),
        )
    }

    // ── Tables ────────────────────────────────────────────────────────────────

    private fun customersTable(relations: List<RelationFacet.Relation>) = table(
        schema = "retail", name = "customers", description = "Registered customers", relations = relations,
        columns = listOf(
            attr("retail", "customers", "id",      0, bigInt(true),  "Surrogate primary key"),
            attr("retail", "customers", "email",   1, string(true),  "Customer e-mail address (unique)"),
            attr("retail", "customers", "name",    2, string(true),  "Full display name"),
            attr("retail", "customers", "country", 3, string(false), "ISO-3166 country code"),
        ),
    )

    private fun productsTable(relations: List<RelationFacet.Relation>) = table(
        schema = "retail", name = "products", description = "Product catalogue", relations = relations,
        columns = listOf(
            attr("retail", "products", "id",       0, bigInt(true),  "Surrogate primary key"),
            attr("retail", "products", "name",     1, string(true),  "Product display name"),
            attr("retail", "products", "category", 2, string(false), "Top-level product category"),
            attr("retail", "products", "price",    3, double(true),  "Unit price in default currency"),
        ),
    )

    private fun ordersTable(relations: List<RelationFacet.Relation>) = table(
        schema = "retail", name = "orders", description = "Customer purchase orders", relations = relations,
        columns = listOf(
            attr("retail", "orders", "id",           0, bigInt(true),     "Surrogate primary key"),
            attr("retail", "orders", "customer_id",  1, bigInt(true),     "FK → customers.id"),
            attr("retail", "orders", "created_at",   2, timestamp(true),  "Order placement timestamp (UTC)"),
            attr("retail", "orders", "total_amount", 3, double(true),     "Pre-computed order total"),
        ),
    )

    private fun orderItemsTable(relations: List<RelationFacet.Relation>) = table(
        schema = "retail", name = "order_items", description = "Individual line items within an order", relations = relations,
        columns = listOf(
            attr("retail", "order_items", "id",         0, bigInt(true),  "Surrogate primary key"),
            attr("retail", "order_items", "order_id",   1, bigInt(true),  "FK → orders.id"),
            attr("retail", "order_items", "product_id", 2, bigInt(true),  "FK → products.id"),
            attr("retail", "order_items", "quantity",   3, int(true),     "Number of units ordered"),
            attr("retail", "order_items", "unit_price", 4, double(true),  "Price per unit at time of purchase"),
        ),
    )

    /** No description — tests the missing-metadata case. */
    private fun rawEventsTable() = table(
        schema = "retail", name = "raw_events", description = null, relations = emptyList(),
        columns = listOf(
            attr("retail", "raw_events", "event_id",    0, string(true),    null),
            attr("retail", "raw_events", "event_type",  1, string(true),    null),
            attr("retail", "raw_events", "payload",     2, string(false),   null),
            attr("retail", "raw_events", "recorded_at", 3, timestamp(true), null),
        ),
    )

    // ── Builder helpers ───────────────────────────────────────────────────────

    private fun table(
        schema: String, name: String, description: String?,
        relations: List<RelationFacet.Relation>,
        columns: List<SchemaColumnWithFacets>,
    ): SchemaTableWithFacets {
        val facetSet = mutableSetOf<MetadataFacet>()
        description?.let { facetSet.add(DescriptiveFacet(description = it)) }
        if (relations.isNotEmpty()) facetSet.add(RelationFacet(relations))
        return SchemaTableWithFacets(
            schemaName = schema, tableName = name,
            tableType = Table.TableTypeId.TABLE,
            columns = columns, metadata = null,
            facets = SchemaFacets(facetSet),
        )
    }

    private fun attr(
        schema: String, table: String, name: String, index: Int,
        dataType: DataType, description: String?,
    ): SchemaColumnWithFacets {
        val facetSet = mutableSetOf<MetadataFacet>()
        description?.let { facetSet.add(DescriptiveFacet(description = it)) }
        return SchemaColumnWithFacets(
            schemaName = schema, tableName = table, columnName = name,
            fieldIndex = index, dataType = dataType, metadata = null,
            facets = SchemaFacets(facetSet),
        )
    }

    @Suppress("LongParameterList")
    private fun rel(
        name: String, description: String,
        source: TableLocator, sourceAttrs: List<String>,
        target: TableLocator, targetAttrs: List<String>,
        cardinality: RelationCardinality, type: RelationType, businessMeaning: String,
    ) = RelationFacet.Relation(
        name = name, description = description,
        sourceTable = source, sourceAttributes = sourceAttrs,
        targetTable = target, targetAttributes = targetAttrs,
        cardinality = cardinality, type = type, businessMeaning = businessMeaning,
    )

    private fun descFacets(description: String) =
        SchemaFacets(setOf(DescriptiveFacet(description = description)))

    // ── DataType factories ────────────────────────────────────────────────────

    private fun bigInt(notNull: Boolean)    = dt(LogicalDataType.LogicalDataTypeId.BIG_INT,   notNull)
    private fun int(notNull: Boolean)       = dt(LogicalDataType.LogicalDataTypeId.INT,        notNull)
    private fun string(notNull: Boolean)    = dt(LogicalDataType.LogicalDataTypeId.STRING,     notNull)
    private fun double(notNull: Boolean)    = dt(LogicalDataType.LogicalDataTypeId.DOUBLE,     notNull)
    private fun timestamp(notNull: Boolean) = dt(LogicalDataType.LogicalDataTypeId.TIMESTAMP,  notNull)

    private fun dt(id: LogicalDataType.LogicalDataTypeId, notNull: Boolean): DataType =
        DataType.newBuilder()
            .setNullability(if (notNull) DataType.Nullability.NOT_NULL else DataType.Nullability.NULL)
            .setType(LogicalDataType.newBuilder().setTypeId(id))
            .build()
}

