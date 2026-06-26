package io.qpointz.mill.ai.core.artifact

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FacetProposalWireTest {

    @Test
    fun shouldNormalizeSchemaCaptureShape_toFacetProposalWire() {
        val wire = FacetProposalWire.normalizePayload(
            mapOf(
                "captureType" to "description",
                "targetEntityId" to "skymill.passenger",
                "serializedPayload" to mapOf("summary" to "Passenger manifest"),
            ),
        )

        assertEquals(
            mapOf(
                "facetTypeKey" to "descriptive",
                "metadataEntityId" to "skymill.passenger",
                "payload" to mapOf("summary" to "Passenger manifest"),
            ),
            wire,
        )
    }

    @Test
    fun shouldPassThroughExistingFacetProposalShape() {
        val wire = FacetProposalWire.normalizePayload(
            mapOf(
                "facetTypeKey" to "relation",
                "metadataEntityId" to "sales.orders",
                "payload" to mapOf("join" to "customer_id"),
            ),
        )

        assertEquals(
            mapOf(
                "facetTypeKey" to "relation",
                "metadataEntityId" to "sales.orders",
                "payload" to mapOf("join" to "customer_id"),
            ),
            wire,
        )
    }

    @Test
    fun shouldIncludeRationaleAndCatalogPath_whenPresent() {
        val wire = FacetProposalWire.normalizePayload(
            mapOf(
                "facetTypeKey" to "descriptive",
                "metadataEntityId" to "skymill.passenger.id",
                "catalogPath" to "skymill.passenger.id",
                "rationale" to "Unique passenger identifier.",
                "payload" to mapOf("description" to "unique passenger identifier"),
            ),
        )

        assertEquals(
            mapOf(
                "facetTypeKey" to "descriptive",
                "metadataEntityId" to "skymill.passenger.id",
                "catalogPath" to "skymill.passenger.id",
                "rationale" to "Unique passenger identifier.",
                "payload" to mapOf("description" to "unique passenger identifier"),
            ),
            wire,
        )
    }

    @Test
    fun shouldReturnNull_whenSchemaCaptureFailed() {
        assertNull(
            FacetProposalWire.normalizePayload(
                mapOf(
                    "captureType" to "description",
                    "targetEntityId" to "missing.table",
                    "captureSucceeded" to false,
                ),
            ),
        )
    }
}
