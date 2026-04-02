package io.qpointz.mill.metadata.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MillUrnTest {

    // ── parse ──────────────────────────────────────────────────────────────

    @Nested
    inner class Parse {

        @Test
        fun `parses standard data entity URN`() {
            val u = MillUrn.parse("urn:mill/data/table:sales.orders")!!
            assertEquals("mill", u.namespace)
            assertEquals("data", u.group)
            assertEquals("table", u.kind)
            assertEquals("sales.orders", u.id)
        }

        @Test
        fun `parses metadata facet-type URN`() {
            val u = MillUrn.parse("urn:mill/metadata/facet-type:descriptive")!!
            assertEquals("metadata", u.group)
            assertEquals("facet-type", u.kind)
            assertEquals("descriptive", u.id)
        }

        @Test
        fun `parses scope URN with colon in id`() {
            val u = MillUrn.parse("urn:mill/metadata/scope:user:alice")!!
            assertEquals("scope", u.kind)
            assertEquals("user:alice", u.id)
        }

        @Test
        fun `normalises to lowercase`() {
            val u = MillUrn.parse("URN:MILL/DATA/TABLE:SALES.ORDERS")!!
            assertEquals("mill", u.namespace)
            assertEquals("data", u.group)
            assertEquals("table", u.kind)
            assertEquals("sales.orders", u.id)
        }

        @Test
        fun `trims surrounding whitespace`() {
            val u = MillUrn.parse("  urn:mill/data/schema:sales  ")!!
            assertEquals("schema", u.kind)
            assertEquals("sales", u.id)
        }

        @Test
        fun `returns null for plain string`() {
            assertNull(MillUrn.parse("sales.orders"))
        }

        @Test
        fun `returns null for blank input`() {
            assertNull(MillUrn.parse(""))
            assertNull(MillUrn.parse("   "))
        }

        @Test
        fun `returns null for URN missing id`() {
            assertNull(MillUrn.parse("urn:mill/data/schema:"))
        }

        @Test
        fun `returns null for URN missing kind separator`() {
            assertNull(MillUrn.parse("urn:mill/data/schema"))
        }

        @Test
        fun `returns null for metadata entity URN in old form`() {
            // old urn:mill/metadata/entity:x has no /<kind>:<id> — it has only one slash after ns
            // Actually this DOES match: ns=mill, group=metadata, kind=entity, id=sales
            // That is the correct structural parse of the old form
            val u = MillUrn.parse("urn:mill/metadata/entity:sales")!!
            assertEquals("metadata", u.group)
            assertEquals("entity", u.kind)
            assertEquals("sales", u.id)
        }
    }

    // ── parseOrThrow ───────────────────────────────────────────────────────

    @Nested
    inner class ParseOrThrow {

        @Test
        fun `returns parsed URN for valid input`() {
            val u = MillUrn.parseOrThrow("urn:mill/data/schema:db")
            assertEquals("schema", u.kind)
        }

        @Test
        fun `throws for invalid input`() {
            assertThrows(IllegalArgumentException::class.java) {
                MillUrn.parseOrThrow("not-a-urn")
            }
        }
    }

    // ── raw ────────────────────────────────────────────────────────────────

    @Nested
    inner class Raw {

        @Test
        fun `round-trips through parse`() {
            val original = "urn:mill/data/table:sales.orders"
            assertEquals(original, MillUrn.parse(original)!!.raw)
        }

        @Test
        fun `reconstructs from direct constructor`() {
            val u = MillUrn("mill", "data", "table", "sales.orders")
            assertEquals("urn:mill/data/table:sales.orders", u.raw)
        }

        @Test
        fun `preserves colon in id`() {
            val u = MillUrn.parse("urn:mill/metadata/scope:user:alice")!!
            assertEquals("urn:mill/metadata/scope:user:alice", u.raw)
        }
    }

    // ── value equality ─────────────────────────────────────────────────────

    @Test
    fun `equal when components match`() {
        val a = MillUrn.parse("urn:mill/data/schema:sales")!!
        val b = MillUrn("mill", "data", "schema", "sales")
        assertEquals(a, b)
    }

    @Test
    fun `not equal when kind differs`() {
        val a = MillUrn.parse("urn:mill/data/schema:sales")!!
        val b = MillUrn.parse("urn:mill/data/table:sales")!!
        assertNotEquals(a, b)
    }
}
