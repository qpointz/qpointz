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
import io.qpointz.mill.data.odata.annotation.ODataVocabularyTerms
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
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
class EntityDataModelFactoryAnnotationTest {

    @Mock
    private lateinit var schemaFacetService: SchemaFacetService

    private lateinit var factory: EntityDataModelFactory

    @BeforeEach
    fun setUp() {
        factory = EntityDataModelFactory(schemaFacetService)
    }

    @Test
    fun shouldBuildAnnotationModelFromTableAndColumnFacets() {
        val cities = SchemaTableWithFacets(
            schemaName = "skymill",
            tableName = "cities",
            tableType = Table.TableTypeId.TABLE,
            columns = listOf(
                column("id", 0),
                column(
                    "city",
                    1,
                    SchemaFacets(setOf(DescriptiveFacet(description = "City name."))),
                ),
            ),
            metadata = null,
            facets = SchemaFacets(
                setOf(DescriptiveFacet(description = "Airport cities served by the airline.")),
            ),
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

        val schemaPackage = factory.buildPackageForSchema("skymill")

        assertThat(schemaPackage.annotations.entityType("cities"))
            .extracting<String> { it.term }
            .contains(ODataVocabularyTerms.CORE_DESCRIPTION)
        assertThat(schemaPackage.annotations.structuralProperty("cities", "city"))
            .extracting<String> { it.stringValue }
            .contains("City name.")
    }

    private fun column(name: String, index: Int, facets: SchemaFacets = SchemaFacets.EMPTY): SchemaColumnWithFacets {
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
            facets = facets,
        )
    }
}
