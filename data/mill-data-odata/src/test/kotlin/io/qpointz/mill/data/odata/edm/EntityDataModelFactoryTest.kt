package io.qpointz.mill.data.odata.edm

import com.sdl.odata.api.edm.model.EntityType
import com.sdl.odata.api.edm.model.NavigationProperty
import io.qpointz.mill.data.metadata.SchemaModelRoot
import io.qpointz.mill.data.schema.ModelRootWithFacets
import io.qpointz.mill.data.schema.SchemaColumnWithFacets
import io.qpointz.mill.data.schema.SchemaFacetResult
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.data.schema.facet.RelationFacet
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.metadata.domain.core.TableLocator
import io.qpointz.mill.metadata.service.MetadataContext
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType
import io.qpointz.mill.proto.Table
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class EntityDataModelFactoryTest {

    @Mock
    private lateinit var schemaFacetService: SchemaFacetService

    private lateinit var factory: EntityDataModelFactory

    @BeforeEach
    fun setUp() {
        factory = EntityDataModelFactory(schemaFacetService)
    }

    @Test
    fun shouldBuildEntitySetAndPropertiesFromSchemaFacetService() {
        val cities = SchemaTableWithFacets(
            schemaName = "skymill",
            tableName = "cities",
            tableType = Table.TableTypeId.TABLE,
            columns = listOf(
                column("id", 0),
                column("city", 1),
            ),
            metadata = null,
            facets = SchemaFacets.EMPTY,
        )
        whenever(schemaFacetService.getSchemas(MetadataContext.global())).thenReturn(
            SchemaFacetResult(
                modelRoot = ModelRootWithFacets(
                    metadataEntityId = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID),
                    metadata = null,
                    facets = SchemaFacets.EMPTY,
                ),
                schemas = listOf(
                    SchemaWithFacets(
                        schemaName = "skymill",
                        tables = listOf(cities),
                        metadata = null,
                        facets = SchemaFacets.EMPTY,
                    ),
                ),
                unboundMetadata = emptyList(),
            ),
        )

        val edm = factory.buildForSchema("skymill")

        assertThat(edm.entityContainer.name).isEqualTo("skymill")
        val entitySet = edm.entityContainer.getEntitySet("cities")
        assertThat(entitySet).isNotNull
        assertThat(entitySet.typeName).isEqualTo("Mill.skymill.cities")

        val entityType = edm.getType("Mill.skymill.cities") as EntityType
        assertThat(entityType.structuralProperties.map { it.name }).containsExactly("id", "city")
    }

    @Test
    fun shouldIncludeNavigationPropertiesFromRelationFacets() {
        val relationFacet =
            RelationFacet(
                relations =
                    listOf(
                        RelationFacet.Relation(
                            name = "origin_segments",
                            sourceTable = TableLocator("skymill", "cities"),
                            sourceAttributes = listOf("id"),
                            targetTable = TableLocator("skymill", "segments"),
                            targetAttributes = listOf("origin"),
                            cardinality = RelationCardinality.ONE_TO_MANY,
                        ),
                    ),
            )
        val cities = SchemaTableWithFacets(
            schemaName = "skymill",
            tableName = "cities",
            tableType = Table.TableTypeId.TABLE,
            columns = listOf(column("id", 0)),
            metadata = null,
            facets = SchemaFacets(setOf(relationFacet)),
        )
        whenever(schemaFacetService.getSchemas(MetadataContext.global())).thenReturn(
            SchemaFacetResult(
                modelRoot = ModelRootWithFacets(
                    metadataEntityId = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID),
                    metadata = null,
                    facets = SchemaFacets.EMPTY,
                ),
                schemas = listOf(
                    SchemaWithFacets(
                        schemaName = "skymill",
                        tables = listOf(cities),
                        metadata = null,
                        facets = SchemaFacets.EMPTY,
                    ),
                ),
                unboundMetadata = emptyList(),
            ),
        )

        val edm = factory.buildForSchema("skymill")
        val entityType = edm.getType("Mill.skymill.cities") as EntityType
        val navNames = entityType.structuralProperties
            .filter { it is NavigationProperty }
            .map { it.name }

        assertThat(navNames).contains("origin_segments")
    }

    @Test
    fun shouldExposeDateColumnsAsDateTimeOffsetInEdm() {
        val cargoFlights = SchemaTableWithFacets(
            schemaName = "skymill",
            tableName = "cargo_flights",
            tableType = Table.TableTypeId.TABLE,
            columns = listOf(
                column("id", 0, LogicalDataType.LogicalDataTypeId.INT),
                column("departure_date", 1, LogicalDataType.LogicalDataTypeId.DATE),
            ),
            metadata = null,
            facets = SchemaFacets.EMPTY,
        )
        whenever(schemaFacetService.getSchemas(MetadataContext.global())).thenReturn(
            SchemaFacetResult(
                modelRoot = ModelRootWithFacets(
                    metadataEntityId = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID),
                    metadata = null,
                    facets = SchemaFacets.EMPTY,
                ),
                schemas = listOf(
                    SchemaWithFacets(
                        schemaName = "skymill",
                        tables = listOf(cargoFlights),
                        metadata = null,
                        facets = SchemaFacets.EMPTY,
                    ),
                ),
                unboundMetadata = emptyList(),
            ),
        )

        val entityType = factory.buildForSchema("skymill").getType("Mill.skymill.cargo_flights") as EntityType
        val departureDate = entityType.structuralProperties.first { it.name == "departure_date" }

        assertThat(departureDate.typeName).isEqualTo("Edm.DateTimeOffset")
    }

    private fun column(name: String, index: Int): SchemaColumnWithFacets =
        column(name, index, LogicalDataType.LogicalDataTypeId.INT)

    private fun column(
        name: String,
        index: Int,
        typeId: LogicalDataType.LogicalDataTypeId,
    ): SchemaColumnWithFacets {
        val dataType = DataType.newBuilder()
            .setType(LogicalDataType.newBuilder().setTypeId(typeId))
            .build()
        return SchemaColumnWithFacets(
            schemaName = "skymill",
            tableName = "cities",
            columnName = name,
            fieldIndex = index,
            dataType = dataType,
            metadata = null,
            facets = SchemaFacets.EMPTY,
        )
    }
}
