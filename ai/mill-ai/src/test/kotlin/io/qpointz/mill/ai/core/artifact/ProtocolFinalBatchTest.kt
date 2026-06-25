package io.qpointz.mill.ai.core.artifact

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProtocolFinalBatchTest {

    @Test
    fun shouldTreatScalarPayload_asSingleItem() {
        val scalar = mapOf("facetTypeKey" to "descriptive", "metadataEntityId" to "a.b")
        assertFalse(ProtocolFinalBatch.isBatchEnvelope(scalar))
        assertEquals(listOf(scalar), ProtocolFinalBatch.expandItemPayloads(scalar))
    }

    @Test
    fun shouldExpandBatchEnvelope_toTwoItems() {
        val a = mapOf("facetTypeKey" to "descriptive", "metadataEntityId" to "a.b")
        val b = mapOf("facetTypeKey" to "dq-null-check", "metadataEntityId" to "a.c")
        val batch = mapOf(ProtocolFinalBatch.RESULTS_FIELD to listOf(a, b))
        assertTrue(ProtocolFinalBatch.isBatchEnvelope(batch))
        assertEquals(listOf(a, b), ProtocolFinalBatch.expandItemPayloads(batch))
    }

    @Test
    fun shouldCollapseTwoItems_toBatchEnvelope() {
        val a = mapOf("facetTypeKey" to "descriptive")
        val b = mapOf("facetTypeKey" to "relation")
        assertEquals(
            mapOf(ProtocolFinalBatch.RESULTS_FIELD to listOf(a, b)),
            ProtocolFinalBatch.collapseResults(listOf(a, b), forceBatch = true),
        )
    }

    @Test
    fun shouldCollapseSingleItem_toScalar_whenNotForced() {
        val a = mapOf("facetTypeKey" to "descriptive")
        assertEquals(a, ProtocolFinalBatch.collapseResults(listOf(a)))
    }
}
