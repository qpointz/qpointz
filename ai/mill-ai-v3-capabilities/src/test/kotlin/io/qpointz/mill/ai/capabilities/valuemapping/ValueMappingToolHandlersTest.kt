package io.qpointz.mill.ai.capabilities.valuemapping

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ValueMappingToolHandlers].
 *
 * Uses a simple in-memory resolver stub so there is no framework or I/O involvement.
 */
class ValueMappingToolHandlersTest {

    // ---------------------------------------------------------------------------
    // In-memory resolver stub
    // ---------------------------------------------------------------------------

    private val knownAttributes = mapOf(
        "MONETA.CLIENTS" to listOf(
            MappedAttribute("STATUS", true),
            MappedAttribute("SEGMENT", true),
            MappedAttribute("NAME", false),
        )
    )

    private val knownMappings = mapOf(
        Triple("MONETA.CLIENTS", "SEGMENT", "ultra")   to "ULTRA",
        Triple("MONETA.CLIENTS", "SEGMENT", "wealth")  to "WEALTH",
        Triple("MONETA.CLIENTS", "STATUS",  "active")  to "ACTIVE",
    )

    private val resolver = object : ValueMappingResolver {
        override fun getMappedAttributes(tableId: String): List<MappedAttribute> =
            knownAttributes[tableId] ?: emptyList()

        override fun resolveValues(
            tableId: String,
            attributeName: String,
            requestedValues: List<String>,
        ): List<ValueResolution> =
            requestedValues.map { term ->
                ValueResolution(
                    requestedValue = term,
                    mappedValue = knownMappings[Triple(tableId, attributeName, term)],
                )
            }
    }

    // ---------------------------------------------------------------------------
    // getMappedAttributes
    // ---------------------------------------------------------------------------

    @Test
    fun `shouldReturnAttributes_whenTableIsKnown`() {
        val result = ValueMappingToolHandlers.getMappedAttributes(resolver, "MONETA.CLIENTS")

        assertEquals("MONETA.CLIENTS", result.table)
        val attrs = result.attributes
        assertEquals(3, attrs.size)
        assertTrue(attrs.any { it.attribute == "STATUS" && it.mapped })
        assertTrue(attrs.any { it.attribute == "SEGMENT" && it.mapped })
        assertTrue(attrs.any { it.attribute == "NAME" && !it.mapped })
    }

    @Test
    fun `shouldReturnEmptyAttributeList_whenTableIsUnknown`() {
        val result = ValueMappingToolHandlers.getMappedAttributes(resolver, "UNKNOWN.TABLE")

        assertEquals("UNKNOWN.TABLE", result.table)
        assertTrue(result.attributes.isEmpty())
    }

    // ---------------------------------------------------------------------------
    // resolveValues
    // ---------------------------------------------------------------------------

    @Test
    fun `shouldReturnMappedValues_whenTermsAreKnown`() {
        val result = ValueMappingToolHandlers.resolveValues(
            resolver,
            tableId = "MONETA.CLIENTS",
            attributeName = "SEGMENT",
            requestedValues = listOf("ultra", "wealth"),
        )

        assertEquals("MONETA.CLIENTS", result.table)
        assertEquals("SEGMENT", result.attribute)
        assertEquals(2, result.results.size)
        assertEquals("ULTRA",  result.results.first { it.requestedValue == "ultra"  }.mappedValue)
        assertEquals("WEALTH", result.results.first { it.requestedValue == "wealth" }.mappedValue)
    }

    @Test
    fun `shouldReturnNullMappedValue_whenTermIsUnknown`() {
        val result = ValueMappingToolHandlers.resolveValues(
            resolver,
            tableId = "MONETA.CLIENTS",
            attributeName = "SEGMENT",
            requestedValues = listOf("vip"),
        )

        assertEquals(1, result.results.size)
        assertNull(result.results[0].mappedValue)
        assertEquals("vip", result.results[0].requestedValue)
    }

    @Test
    fun `shouldReturnPartialResults_whenMixedKnownAndUnknownTerms`() {
        val result = ValueMappingToolHandlers.resolveValues(
            resolver,
            tableId = "MONETA.CLIENTS",
            attributeName = "SEGMENT",
            requestedValues = listOf("ultra", "unknown-tier"),
        )

        assertEquals(2, result.results.size)
        assertEquals("ULTRA", result.results.first { it.requestedValue == "ultra"         }.mappedValue)
        assertNull(           result.results.first { it.requestedValue == "unknown-tier"  }.mappedValue)
    }
}
