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
}
