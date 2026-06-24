package io.qpointz.mill.data.schema

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RelationPayloadNormalizationTest {

    @Test
    fun `bootstrap nested source target maps to RelationFacet wire shape`() {
        val payload =
            mapOf(
                "source" to
                    mapOf(
                        "schema" to "skymill",
                        "table" to "aircraft",
                        "columns" to listOf("id"),
                    ),
                "target" to
                    mapOf(
                        "schema" to "skymill",
                        "table" to "cargo_flights",
                        "columns" to listOf("aircraft_id"),
                    ),
                "cardinality" to "ONE_TO_MANY",
                "joinSql" to "aircraft.id = cargo_flights.aircraft_id",
            )
        val norm = RelationPayloadNormalization.normalizeToRelationPayload(payload)
        @Suppress("UNCHECKED_CAST")
        val relations = norm["relations"] as List<Map<String, Any?>>
        assertEquals(1, relations.size)
        val r = relations[0]
        assertEquals(mapOf("schema" to "skymill", "table" to "aircraft"), r["sourceTable"])
        assertEquals(mapOf("schema" to "skymill", "table" to "cargo_flights"), r["targetTable"])
        assertEquals(listOf("id"), r["sourceAttributes"])
        assertEquals(listOf("aircraft_id"), r["targetAttributes"])
        assertEquals("aircraft.id = cargo_flights.aircraft_id", r["joinSql"])
    }

    @Test
    fun `relations array normalizes each bootstrap entry`() {
        val payload =
            mapOf(
                "relations" to
                    listOf(
                        mapOf(
                            "source" to
                                mapOf(
                                    "schema" to "a",
                                    "table" to "t1",
                                    "columns" to listOf("x"),
                                ),
                            "target" to
                                mapOf(
                                    "schema" to "a",
                                    "table" to "t2",
                                    "columns" to listOf("y"),
                                ),
                            "cardinality" to "ONE_TO_MANY",
                        ),
                    ),
            )
        val norm = RelationPayloadNormalization.normalizeToRelationPayload(payload)
        @Suppress("UNCHECKED_CAST")
        val relations = norm["relations"] as List<Map<String, Any?>>
        assertEquals(1, relations.size)
        assertEquals(mapOf("schema" to "a", "table" to "t1"), relations[0]["sourceTable"])
    }

    @Test
    fun `relation-source maps owner table to source`() {
        val payload =
            mapOf(
                "sourceColumns" to listOf("id"),
                "target" to
                    mapOf(
                        "schema" to "skymill",
                        "table" to "segments",
                        "columns" to listOf("origin"),
                    ),
                "cardinality" to "ONE_TO_MANY",
            )
        val relation =
            RelationPayloadNormalization.relationSourceToRelation(payload, "skymill", "cities")
                ?: error("expected relation")
        assertEquals(mapOf("schema" to "skymill", "table" to "cities"), relation["sourceTable"])
        assertEquals(mapOf("schema" to "skymill", "table" to "segments"), relation["targetTable"])
        assertEquals(listOf("id"), relation["sourceAttributes"])
        assertEquals(listOf("origin"), relation["targetAttributes"])
        assertEquals("origin_segments", relation["name"])
    }

    @Test
    fun `relation-target flips to owner-as-source and inverts cardinality`() {
        val payload =
            mapOf(
                "source" to
                    mapOf(
                        "schema" to "skymill",
                        "table" to "cities",
                        "columns" to listOf("id"),
                    ),
                "targetColumns" to listOf("origin"),
                "cardinality" to "ONE_TO_MANY",
            )
        val relation =
            RelationPayloadNormalization.relationTargetToRelation(payload, "skymill", "segments")
                ?: error("expected relation")
        assertEquals(mapOf("schema" to "skymill", "table" to "segments"), relation["sourceTable"])
        assertEquals(mapOf("schema" to "skymill", "table" to "cities"), relation["targetTable"])
        assertEquals(listOf("origin"), relation["sourceAttributes"])
        assertEquals(listOf("id"), relation["targetAttributes"])
        assertEquals("MANY_TO_ONE", relation["cardinality"])
        assertEquals("origin_cities", relation["name"])
    }

    @Test
    fun `invertCardinality swaps one-to-many and many-to-one`() {
        assertEquals("MANY_TO_ONE", RelationPayloadNormalization.invertCardinality("ONE_TO_MANY"))
        assertEquals("ONE_TO_MANY", RelationPayloadNormalization.invertCardinality("MANY_TO_ONE"))
        assertEquals("ONE_TO_ONE", RelationPayloadNormalization.invertCardinality("ONE_TO_ONE"))
    }
}
