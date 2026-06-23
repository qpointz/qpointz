package io.qpointz.mill.data.odata.edm

import com.sdl.odata.api.edm.model.EntityType
import io.qpointz.mill.data.metadata.SchemaModelRoot
import io.qpointz.mill.data.schema.ModelRootWithFacets
import io.qpointz.mill.data.schema.SchemaColumnWithFacets
import io.qpointz.mill.data.schema.SchemaFacetResult
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
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

    private fun column(name: String, index: Int): SchemaColumnWithFacets {
        val dataType = DataType.newBuilder()
            .setType(LogicalDataType.newBuilder().setTypeId(LogicalDataType.LogicalDataTypeId.INT))
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
