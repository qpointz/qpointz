package io.qpointz.mill.ai.capabilities.valuemapping

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValueMappingToolHandlersTest {

    @Test
    fun `resolveValues echoes requested term as mappedValue when resolver returns null mapping`() {
        val resolver =
            object : ValueMappingResolver {
                override fun getMappedAttributes(tableId: String): List<MappedAttribute> = emptyList()

                override fun resolveValues(
                    tableId: String,
                    attributeName: String,
                    requestedValues: List<String>,
                ): List<ValueResolution> =
                    requestedValues.map { ValueResolution(requestedValue = it, mappedValue = null) }
            }

        val out =
            ValueMappingToolHandlers.resolveValues(resolver, "MONETA.CLIENTS", "city", listOf("Boston"))

        assertEquals(1, out.results.size)
        assertEquals("Boston", out.results[0].requestedValue)
        assertEquals("Boston", out.results[0].mappedValue)
    }

    @Test
    fun `resolveValues preserves canonical mapped value when resolver finds a match`() {
        val resolver =
            object : ValueMappingResolver {
                override fun getMappedAttributes(tableId: String): List<MappedAttribute> = emptyList()

                override fun resolveValues(
                    tableId: String,
                    attributeName: String,
                    requestedValues: List<String>,
                ): List<ValueResolution> =
                    listOf(ValueResolution(requestedValue = "Boston", mappedValue = "BOS", similarityScore = 0.91))
            }

        val out =
            ValueMappingToolHandlers.resolveValues(resolver, "MONETA.CLIENTS", "city", listOf("Boston"))

        assertEquals("BOS", out.results.single().mappedValue)
        assertEquals(0.91, out.results.single().similarityScore)
    }
}
