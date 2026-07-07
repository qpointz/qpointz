package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.capabilities.concept.ConceptCatalogPort
import io.qpointz.mill.ai.capabilities.concept.ConceptDetail
import io.qpointz.mill.ai.capabilities.concept.ConceptSummary
import io.qpointz.mill.ai.capabilities.concept.ConceptTagCount
import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.ai.capabilities.schema.ListColumnsItem
import io.qpointz.mill.ai.capabilities.schema.ListRelationsItem
import io.qpointz.mill.ai.capabilities.schema.ListSchemasItem
import io.qpointz.mill.ai.capabilities.schema.ListTablesItem
import io.qpointz.mill.ai.capabilities.schema.RelationDirection
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.sqlquery.mockSqlQueryCapabilityDependency
import io.qpointz.mill.ai.capabilities.valuemapping.MockValueMappingResolver
import io.qpointz.mill.ai.dependencies.SchemaFacingCapabilityDependencyFactory
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType
import io.qpointz.mill.sql.v2.dialect.DialectRegistry

/**
 * Test collaborators for scenario packs that exercise schema-facing profiles.
 */
object ScenarioHarnessSupport {

    private val skymillCatalog = SkymillScenarioCatalog()

    /** Metadata port with harness facet catalog (≥5 types per GAPS §12). */
    val metadataReadPort: MetadataReadPort = HarnessMetadataReadPort()

    private val conceptCatalog: ConceptCatalogPort = SkymillScenarioConceptCatalog()

    private val dialectSpec = DialectRegistry.fromClasspathDefaults().requireDialect("calcite")

    /**
     * Builds an [AgentContext] with stub schema/metadata/SQL dependencies for [profile].
     *
     * @param profile Active scenario profile.
     */
    fun agentContext(profile: AgentProfile): AgentContext = AgentContext(
        contextType = "general",
        capabilityDependencies = SchemaFacingCapabilityDependencyFactory.build(
            profile = profile,
            schemaCatalog = skymillCatalog,
            metadataReadPort = metadataReadPort,
            conceptCatalog = conceptCatalog,
            dialectSpec = dialectSpec,
            sqlQueryDependency = mockSqlQueryCapabilityDependency(),
            valueMappingResolver = MockValueMappingResolver(),
        ),
    )
}

private class SkymillScenarioCatalog : SchemaCatalogPort {

    private val schema = "skymill"

    private val tables = listOf(
        table("cities", "Airport cities and geographic reference data."),
        table("segments", "Origin-destination route segments between cities."),
        table("passenger", "Registered passengers with loyalty enrollment and tier attributes."),
        table("flight_instances", "Concrete passenger flights on aircraft and route segments."),
        table("bookings", "Passenger bookings linking passengers to flight instances."),
        table("ticket_prices", "Ticket price, taxes, total price, class, and currency per booking."),
        table("ratings", "Passenger satisfaction ratings for completed flights."),
        table("loyalty_earnings", "Mileage earned by passenger bookings."),
        table("delays", "Passenger flight delay minutes and reasons."),
        table("cancellations", "Cancelled passenger flights and cancellation reasons."),
        table("cargo_flights", "Concrete cargo flights on aircraft and route segments."),
        table("cargo_shipments", "Cargo shipment weight and revenue by flight, type, and client."),
        table("cargo_clients", "Cargo customer organizations and their country/region."),
        table("cargo_types", "Cargo category dimension."),
        table("countries", "Country reference data for cargo clients."),
    )

    private val columns = mapOf(
        "passenger" to listOf(
            column("passenger", "id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NOT_NULL, "Passenger identifier."),
            column("passenger", "loyalty_program_member", LogicalDataType.LogicalDataTypeId.BOOL, DataType.Nullability.NULL, "Whether the passenger is enrolled in loyalty."),
            column("passenger", "loyalty_tier", LogicalDataType.LogicalDataTypeId.STRING, DataType.Nullability.NULL, "Loyalty tier: basic, silver, gold, or platinum."),
        ),
        "bookings" to listOf(
            column("bookings", "id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NOT_NULL, "Booking identifier."),
            column("bookings", "passenger_id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NOT_NULL, "Passenger on the booking."),
            column("bookings", "flight_instance_id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NOT_NULL, "Booked passenger flight."),
        ),
        "ticket_prices" to listOf(
            column("ticket_prices", "booking_id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NOT_NULL, "Booking priced by this row."),
            column("ticket_prices", "total_price", LogicalDataType.LogicalDataTypeId.DOUBLE, DataType.Nullability.NULL, "Final ticket price."),
            column("ticket_prices", "travel_class", LogicalDataType.LogicalDataTypeId.STRING, DataType.Nullability.NULL, "Cabin class."),
        ),
        "delays" to listOf(
            column("delays", "flight_instance_id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NOT_NULL, "Delayed passenger flight."),
            column("delays", "delay_minutes", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NULL, "Delay duration in minutes."),
            column("delays", "reason", LogicalDataType.LogicalDataTypeId.STRING, DataType.Nullability.NULL, "Delay cause."),
        ),
        "cargo_shipments" to listOf(
            column("cargo_shipments", "cargo_flight_id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NOT_NULL, "Cargo flight carrying the shipment."),
            column("cargo_shipments", "cargo_type_id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NOT_NULL, "Cargo type."),
            column("cargo_shipments", "weight_kg", LogicalDataType.LogicalDataTypeId.DOUBLE, DataType.Nullability.NULL, "Shipment weight in kilograms."),
            column("cargo_shipments", "revenue", LogicalDataType.LogicalDataTypeId.DOUBLE, DataType.Nullability.NULL, "Shipment revenue."),
        ),
        "cargo_clients" to listOf(
            column("cargo_clients", "id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NOT_NULL, "Cargo client identifier."),
            column("cargo_clients", "country_id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NULL, "Client country."),
            column("cargo_clients", "region", LogicalDataType.LogicalDataTypeId.STRING, DataType.Nullability.NULL, "Client region."),
        ),
        "ratings" to listOf(
            column("ratings", "flight_instance_id", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NOT_NULL, "Rated passenger flight."),
            column("ratings", "rating", LogicalDataType.LogicalDataTypeId.INT, DataType.Nullability.NULL, "Satisfaction score from 1 to 5."),
            column("ratings", "rated_at", LogicalDataType.LogicalDataTypeId.STRING, DataType.Nullability.NULL, "Rating timestamp."),
        ),
    )

    override fun listSchemas(): List<ListSchemasItem> = listOf(
        ListSchemasItem(
            schemaName = schema,
            description = "Skymill Airlines passenger and cargo demo dataset.",
            displayName = "Skymill Airlines",
            metadataEntityUrn = "urn:mill/model/schema:skymill",
        ),
    )

    override fun listTables(schemaName: String): List<ListTablesItem> =
        if (schemaName.equals(schema, ignoreCase = true)) tables else emptyList()

    override fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem> =
        if (schemaName.equals(schema, ignoreCase = true)) columns[tableName] ?: emptyList() else emptyList()

    override fun listRelations(
        schemaName: String,
        tableName: String,
        direction: RelationDirection,
    ): List<ListRelationsItem> {
        if (!schemaName.equals(schema, ignoreCase = true)) return emptyList()
        return relations.filter {
            direction == RelationDirection.BOTH ||
                direction == RelationDirection.OUTBOUND && it.sourceTable == tableName ||
                direction == RelationDirection.INBOUND && it.targetTable == tableName
        }
    }

    private val relations = listOf(
        relation("bookings", listOf("passenger_id"), "passenger", listOf("id")),
        relation("bookings", listOf("flight_instance_id"), "flight_instances", listOf("id")),
        relation("ticket_prices", listOf("booking_id"), "bookings", listOf("id")),
        relation("delays", listOf("flight_instance_id"), "flight_instances", listOf("id")),
        relation("ratings", listOf("flight_instance_id"), "flight_instances", listOf("id")),
        relation("cargo_shipments", listOf("client_id"), "cargo_clients", listOf("id")),
        relation("cargo_clients", listOf("country_id"), "countries", listOf("id")),
    )

    private fun table(name: String, description: String): ListTablesItem =
        ListTablesItem(
            schemaName = schema,
            tableName = name,
            description = description,
            catalogPath = "$schema.$name",
            metadataEntityUrn = "urn:mill/model/table:$schema.$name",
        )

    private fun column(
        table: String,
        name: String,
        type: LogicalDataType.LogicalDataTypeId,
        nullable: DataType.Nullability,
        description: String,
    ): ListColumnsItem =
        ListColumnsItem(
            schemaName = schema,
            tableName = table,
            columnName = name,
            description = description,
            nullable = nullable,
            type = type,
            catalogPath = "$schema.$table.$name",
            metadataEntityUrn = "urn:mill/model/attribute:$schema.$table.$name",
        )

    private fun relation(
        sourceTable: String,
        sourceAttributes: List<String>,
        targetTable: String,
        targetAttributes: List<String>,
    ): ListRelationsItem =
        ListRelationsItem(
            sourceSchema = schema,
            sourceTable = sourceTable,
            sourceAttributes = sourceAttributes,
            targetSchema = schema,
            targetTable = targetTable,
            targetAttributes = targetAttributes,
            name = "${sourceTable}_to_${targetTable}",
            description = "$sourceTable joins to $targetTable.",
            cardinality = RelationCardinality.MANY_TO_ONE,
            joinSql = "$schema.$sourceTable.${sourceAttributes.first()} = $schema.$targetTable.${targetAttributes.first()}",
        )
}

private class SkymillScenarioConceptCatalog : ConceptCatalogPort {
    private val concepts = listOf(
        ConceptDetail(
            conceptRef = "urn:mill/model/concept:premium-passenger",
            slug = "premium-passenger",
            name = "Premium Passenger",
            description = "A loyalty passenger in gold or platinum tier.",
            sql = "SELECT * FROM skymill.passenger WHERE loyalty_program_member = TRUE AND loyalty_tier IN ('gold', 'platinum')",
            tags = listOf("passenger", "loyalty"),
            facetUid = "scenario-premium-passenger",
        ),
    )

    override fun listConceptTags(scope: String?): List<ConceptTagCount> =
        concepts.flatMap { it.tags }.groupingBy { it }.eachCount().map { ConceptTagCount(it.key, it.value) }

    override fun listConcepts(tag: String?, scope: String?): List<ConceptSummary> =
        concepts.filter { concept -> tag.isNullOrBlank() || concept.tags.any { it.equals(tag, ignoreCase = true) } }
            .map { it.toSummary() }

    override fun getConcept(conceptRef: String, scope: String?): ConceptDetail? =
        concepts.firstOrNull { it.conceptRef == conceptRef }

    override fun searchConcepts(query: String, tag: String?, scope: String?): List<ConceptSummary> =
        listConcepts(tag, scope).filter {
            it.name.contains(query, ignoreCase = true) ||
                it.description?.contains(query, ignoreCase = true) == true ||
                it.tags.any { tagValue -> tagValue.contains(query, ignoreCase = true) }
        }

    override fun getModelConcepts(scope: String?): List<ConceptDetail> = concepts
}
