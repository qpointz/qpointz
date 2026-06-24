package io.qpointz.mill.data.odata.edm

import com.sdl.odata.api.edm.model.NavigationProperty
import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.facet.RelationFacet
import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.metadata.domain.core.TableLocator
import io.qpointz.mill.proto.Table
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NavigationPropertyBuilderTest {

    private val builder = NavigationPropertyBuilder()

    @Test
    fun shouldBuildCollectionNavigationForOneToManyFromCities() {
        val cities = tableWithRelations(
            "cities",
            relation(
                source = TableLocator("skymill", "cities"),
                target = TableLocator("skymill", "segments"),
                sourceAttrs = listOf("id"),
                targetAttrs = listOf("origin"),
                name = "origin_segments",
                cardinality = RelationCardinality.ONE_TO_MANY,
            ),
        )

        val navs = builder.buildForTable(cities, cities.facets.relation!!.relations)

        assertThat(navs).hasSize(1)
        assertThat(navs[0].name).isEqualTo("origin_segments")
        assertThat(navs[0].typeName).isEqualTo("Collection(Mill.skymill.segments)")
    }

    @Test
    fun shouldBuildSingleNavigationForManyToOneFromSegments() {
        val segments = tableWithRelations(
            "segments",
            relation(
                source = TableLocator("skymill", "segments"),
                target = TableLocator("skymill", "cities"),
                sourceAttrs = listOf("origin"),
                targetAttrs = listOf("id"),
                name = "origin_cities",
                cardinality = RelationCardinality.MANY_TO_ONE,
            ),
        )

        val navs = builder.buildForTable(segments, segments.facets.relation!!.relations)

        assertThat(navs).hasSize(1)
        assertThat(navs[0].name).isEqualTo("origin_cities")
        assertThat(navs[0].typeName).isEqualTo("Mill.skymill.cities")
    }

    private fun tableWithRelations(tableName: String, vararg relations: RelationFacet.Relation): SchemaTableWithFacets =
        SchemaTableWithFacets(
            schemaName = "skymill",
            tableName = tableName,
            tableType = Table.TableTypeId.TABLE,
            columns = emptyList(),
            metadata = null,
            facets = SchemaFacets(setOf(RelationFacet(relations = relations.toList()))),
        )

    private fun relation(
        source: TableLocator,
        target: TableLocator,
        sourceAttrs: List<String>,
        targetAttrs: List<String>,
        name: String,
        cardinality: RelationCardinality,
    ): RelationFacet.Relation =
        RelationFacet.Relation(
            name = name,
            sourceTable = source,
            targetTable = target,
            sourceAttributes = sourceAttrs,
            targetAttributes = targetAttrs,
            cardinality = cardinality,
        )
}
