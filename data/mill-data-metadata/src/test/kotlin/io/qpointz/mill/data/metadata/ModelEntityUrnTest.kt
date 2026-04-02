package io.qpointz.mill.data.metadata

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ModelEntityUrnTest {

    // ── forSchema ──────────────────────────────────────────────────────────

    @Nested
    inner class ForSchema {
        @Test
        fun `produces typed schema URN`() {
            assertEquals("urn:mill/model/schema:sales", ModelEntityUrn.forSchema("sales"))
        }

        @Test
        fun `lowercases schema name`() {
            assertEquals("urn:mill/model/schema:sales", ModelEntityUrn.forSchema("SALES"))
        }

        @Test
        fun `trims whitespace`() {
            assertEquals("urn:mill/model/schema:sales", ModelEntityUrn.forSchema("  sales  "))
        }

        @Test
        fun `rejects blank schema`() {
            assertThrows(IllegalArgumentException::class.java) { ModelEntityUrn.forSchema("") }
        }
    }

    // ── forTable ───────────────────────────────────────────────────────────

    @Nested
    inner class ForTable {
        @Test
        fun `produces typed table URN`() {
            assertEquals("urn:mill/model/table:sales.orders", ModelEntityUrn.forTable("sales", "orders"))
        }

        @Test
        fun `lowercases both segments`() {
            assertEquals("urn:mill/model/table:sales.orders", ModelEntityUrn.forTable("SALES", "ORDERS"))
        }

        @Test
        fun `rejects blank table`() {
            assertThrows(IllegalArgumentException::class.java) { ModelEntityUrn.forTable("sales", "") }
        }
    }

    // ── forAttribute ───────────────────────────────────────────────────────

    @Nested
    inner class ForAttribute {
        @Test
        fun `produces typed attribute URN`() {
            assertEquals(
                "urn:mill/model/attribute:sales.orders.order_id",
                ModelEntityUrn.forAttribute("sales", "orders", "order_id"),
            )
        }

        @Test
        fun `lowercases all segments`() {
            assertEquals(
                "urn:mill/model/attribute:sales.orders.order_id",
                ModelEntityUrn.forAttribute("SALES", "ORDERS", "ORDER_ID"),
            )
        }

        @Test
        fun `rejects blank column`() {
            assertThrows(IllegalArgumentException::class.java) {
                ModelEntityUrn.forAttribute("sales", "orders", "")
            }
        }
    }

    // ── forConcept ─────────────────────────────────────────────────────────

    @Nested
    inner class ForConcept {
        @Test
        fun `produces typed concept URN`() {
            assertEquals("urn:mill/model/concept:product-category", ModelEntityUrn.forConcept("product-category"))
        }

        @Test
        fun `rejects blank id`() {
            assertThrows(IllegalArgumentException::class.java) { ModelEntityUrn.forConcept("") }
        }
    }

    // ── MODEL_ENTITY_ID ────────────────────────────────────────────────────

    @Test
    fun `MODEL_ENTITY_ID is canonical model group URN`() {
        assertEquals("urn:mill/model/model:model-entity", ModelEntityUrn.MODEL_ENTITY_ID)
        assertTrue(ModelEntityUrn.isModelRootUrn(ModelEntityUrn.MODEL_ENTITY_ID))
    }

    // ── isModelEntityUrn ───────────────────────────────────────────────────

    @Nested
    inner class IsModelEntityUrn {
        @Test
        fun `recognises model group URNs`() {
            assertTrue(ModelEntityUrn.isModelEntityUrn("urn:mill/model/schema:sales"))
            assertTrue(ModelEntityUrn.isModelEntityUrn("urn:mill/model/table:sales.orders"))
        }

        @Test
        fun `rejects data group URNs`() {
            assertFalse(ModelEntityUrn.isModelEntityUrn("urn:mill/data/table:sales.orders"))
        }

        @Test
        fun `rejects metadata group URNs`() {
            assertFalse(ModelEntityUrn.isModelEntityUrn("urn:mill/metadata/entity:sales"))
        }

        @Test
        fun `rejects plain strings`() {
            assertFalse(ModelEntityUrn.isModelEntityUrn("sales.orders"))
        }

        @Test
        fun `case-insensitive check`() {
            assertTrue(ModelEntityUrn.isModelEntityUrn("URN:MILL/MODEL/SCHEMA:SALES"))
        }
    }

    // ── kindOf ─────────────────────────────────────────────────────────────

    @Nested
    inner class KindOf {
        @Test
        fun `extracts schema`() {
            assertEquals("schema", ModelEntityUrn.kindOf(ModelEntityUrn.forSchema("sales")))
        }

        @Test
        fun `extracts table`() {
            assertEquals("table", ModelEntityUrn.kindOf(ModelEntityUrn.forTable("sales", "orders")))
        }

        @Test
        fun `extracts attribute`() {
            assertEquals("attribute", ModelEntityUrn.kindOf(ModelEntityUrn.forAttribute("sales", "orders", "id")))
        }

        @Test
        fun `extracts model`() {
            assertEquals("model", ModelEntityUrn.kindOf(ModelEntityUrn.MODEL_ENTITY_ID))
        }

        @Test
        fun `extracts concept`() {
            assertEquals("concept", ModelEntityUrn.kindOf(ModelEntityUrn.forConcept("product")))
        }

        @Test
        fun `returns null for non-model group URN`() {
            assertNull(ModelEntityUrn.kindOf("urn:mill/metadata/entity:sales"))
        }

        @Test
        fun `returns null for plain string`() {
            assertNull(ModelEntityUrn.kindOf("sales.orders"))
        }

        @Test
        fun `case-insensitive input`() {
            assertEquals("schema", ModelEntityUrn.kindOf("URN:MILL/MODEL/SCHEMA:SALES"))
        }
    }

    // ── parseCatalogPath ───────────────────────────────────────────────────

    @Nested
    inner class ParseCatalogPath {
        @Test
        fun `parses schema URN`() {
            val path = ModelEntityUrn.parseCatalogPath(ModelEntityUrn.forSchema("sales"))
            assertEquals("sales", path.schema)
            assertNull(path.table)
            assertNull(path.column)
        }

        @Test
        fun `parses table URN`() {
            val path = ModelEntityUrn.parseCatalogPath(ModelEntityUrn.forTable("sales", "orders"))
            assertEquals("sales", path.schema)
            assertEquals("orders", path.table)
            assertNull(path.column)
        }

        @Test
        fun `parses attribute URN`() {
            val path = ModelEntityUrn.parseCatalogPath(ModelEntityUrn.forAttribute("sales", "orders", "order_id"))
            assertEquals("sales", path.schema)
            assertEquals("orders", path.table)
            assertEquals("order_id", path.column)
        }

        @Test
        fun `preserves dots within column segment`() {
            val urn = ModelEntityUrn.forAttribute("db", "tbl", "col.part")
            val path = ModelEntityUrn.parseCatalogPath(urn)
            assertEquals("db", path.schema)
            assertEquals("tbl", path.table)
            assertEquals("col.part", path.column)
        }

        @Test
        fun `model root URN yields empty path`() {
            val path = ModelEntityUrn.parseCatalogPath(ModelEntityUrn.MODEL_ENTITY_ID)
            assertNull(path.schema)
            assertNull(path.table)
            assertNull(path.column)
        }

        @Test
        fun `concept URN yields empty path`() {
            val path = ModelEntityUrn.parseCatalogPath(ModelEntityUrn.forConcept("product"))
            assertNull(path.schema)
        }

        @Test
        fun `non-model group URN yields empty path`() {
            val path = ModelEntityUrn.parseCatalogPath("urn:mill/metadata/entity:sales.orders")
            assertNull(path.schema)
        }

        @Test
        fun `malformed string yields empty path`() {
            val path = ModelEntityUrn.parseCatalogPath("not-a-urn")
            assertNull(path.schema)
        }

        @Test
        fun `case-insensitive input normalised`() {
            val path = ModelEntityUrn.parseCatalogPath("URN:MILL/MODEL/TABLE:SALES.ORDERS")
            assertEquals("sales", path.schema)
            assertEquals("orders", path.table)
        }
    }

    // ── Predicates ─────────────────────────────────────────────────────────

    @Nested
    inner class Predicates {
        @Test
        fun `isSchemaUrn`() {
            assertTrue(ModelEntityUrn.isSchemaUrn(ModelEntityUrn.forSchema("db")))
            assertFalse(ModelEntityUrn.isSchemaUrn(ModelEntityUrn.forTable("db", "tbl")))
        }

        @Test
        fun `isTableUrn`() {
            assertTrue(ModelEntityUrn.isTableUrn(ModelEntityUrn.forTable("db", "tbl")))
            assertFalse(ModelEntityUrn.isTableUrn(ModelEntityUrn.forSchema("db")))
        }

        @Test
        fun `isAttributeUrn`() {
            assertTrue(ModelEntityUrn.isAttributeUrn(ModelEntityUrn.forAttribute("db", "tbl", "col")))
            assertFalse(ModelEntityUrn.isAttributeUrn(ModelEntityUrn.forTable("db", "tbl")))
        }

        @Test
        fun `isModelRootUrn`() {
            assertTrue(ModelEntityUrn.isModelRootUrn(ModelEntityUrn.MODEL_ENTITY_ID))
            assertFalse(ModelEntityUrn.isModelRootUrn(ModelEntityUrn.forSchema("db")))
        }

        @Test
        fun `isConceptUrn`() {
            assertTrue(ModelEntityUrn.isConceptUrn(ModelEntityUrn.forConcept("cat")))
            assertFalse(ModelEntityUrn.isConceptUrn(ModelEntityUrn.forSchema("db")))
        }
    }
}
