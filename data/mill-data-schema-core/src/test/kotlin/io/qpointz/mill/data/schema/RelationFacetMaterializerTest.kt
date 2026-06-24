package io.qpointz.mill.data.schema

import io.qpointz.mill.data.schema.facet.RelationFacet
import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.metadata.domain.core.TableLocator
import io.qpointz.mill.proto.Table
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RelationFacetMaterializerTest {

    @Test
    fun shouldMergeTableAndSchemaLevelRelations() {
        val tableRelation =
            relation(
                source = TableLocator("skymill", "cities"),
                target = TableLocator("skymill", "segments"),
                sourceAttrs = listOf("id"),
                targetAttrs = listOf("origin"),
                name = "origin_segments",
            )
        val schemaRelation =
            relation(
                source = TableLocator("skymill", "cities"),
                target = TableLocator("skymill", "passenger"),
                sourceAttrs = listOf("id"),
                targetAttrs = listOf("domicile_city_id"),
                name = "cities_passenger",
            )
        val cities = table("cities", tableRelation)
        val schema = SchemaWithFacets(
            schemaName = "skymill",
            tables = listOf(cities),
            metadata = null,
            facets = SchemaFacets(setOf(RelationFacet(relations = listOf(schemaRelation)))),
        )

        val effective = RelationFacetMaterializer.effectiveRelations("skymill", cities, schema)

        assertEquals(2, effective.size)
        assertTrue(effective.map { it.name }.containsAll(listOf("origin_segments", "cities_passenger")))
    }

    @Test
    fun shouldDropCrossSchemaRelations() {
        val crossSchema =
            relation(
                source = TableLocator("skymill", "cities"),
                target = TableLocator("other", "places"),
                sourceAttrs = listOf("id"),
                targetAttrs = listOf("city_id"),
            )
        val cities = table("cities", crossSchema)

        val effective = RelationFacetMaterializer.effectiveRelations("skymill", cities, null)

        assertTrue(effective.isEmpty())
    }

    @Test
    fun shouldDeduplicateIdenticalRelations() {
        val edge =
            relation(
                source = TableLocator("skymill", "cities"),
                target = TableLocator("skymill", "segments"),
                sourceAttrs = listOf("id"),
                targetAttrs = listOf("origin"),
                name = "a",
            )
        val duplicate =
            relation(
                source = TableLocator("skymill", "cities"),
                target = TableLocator("skymill", "segments"),
                sourceAttrs = listOf("id"),
                targetAttrs = listOf("origin"),
                name = "b",
            )
        val cities = table("cities", edge, duplicate)

        val effective = RelationFacetMaterializer.effectiveRelations("skymill", cities, null)

        assertEquals(1, effective.size)
    }

    private fun table(name: String, vararg relations: RelationFacet.Relation): SchemaTableWithFacets =
        SchemaTableWithFacets(
            schemaName = "skymill",
            tableName = name,
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
        name: String? = null,
        cardinality: RelationCardinality = RelationCardinality.ONE_TO_MANY,
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
