package io.qpointz.mill.source

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RecordTest {

    @Test
    fun shouldCreateRecord_whenUsingOfFactory() {
        val record = Record.of("id" to 1, "name" to "Alice")
        assertEquals(1, record["id"])
        assertEquals("Alice", record["name"])
    }

    @Test
    fun shouldReturnNull_whenKeyNotPresent() {
        val record = Record.of("id" to 1)
        assertNull(record["missing"])
    }

    @Test
    fun shouldStoreNullValues_whenExplicitlySet() {
        val record = Record.of("id" to 1, "name" to null)
        assertTrue(record.values.containsKey("name"))
        assertNull(record["name"])
    }

    @Test
    fun shouldCreateEmptyRecord_whenNoArgs() {
        val record = Record.empty()
        assertTrue(record.values.isEmpty())
    }

    @Test
    fun shouldCreateRecord_whenUsingMapConstructor() {
        val map = mapOf("a" to 10, "b" to 20)
        val record = Record(map)
        assertEquals(10, record["a"])
        assertEquals(20, record["b"])
    }

    @Test
    fun shouldSupportDataClassEquality() {
        val r1 = Record.of("id" to 1, "name" to "Alice")
        val r2 = Record.of("id" to 1, "name" to "Alice")
        assertEquals(r1, r2)
        assertEquals(r1.hashCode(), r2.hashCode())
    }

    @Test
    fun shouldNotBeEqual_whenValuesDiffer() {
        val r1 = Record.of("id" to 1)
        val r2 = Record.of("id" to 2)
        assertNotEquals(r1, r2)
    }

    @Test
    fun shouldSupportMixedValueTypes() {
        val record = Record.of(
            "int" to 42,
            "string" to "hello",
            "double" to 3.14,
            "bool" to true,
            "null" to null
        )
        assertEquals(42, record["int"])
        assertEquals("hello", record["string"])
        assertEquals(3.14, record["double"])
        assertEquals(true, record["bool"])
        assertNull(record["null"])
    }
}
