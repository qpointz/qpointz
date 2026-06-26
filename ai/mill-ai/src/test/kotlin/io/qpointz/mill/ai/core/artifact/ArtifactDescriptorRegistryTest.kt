package io.qpointz.mill.ai.core.artifact

import io.qpointz.mill.ai.core.artifact.PointerCardinality
import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.protocol.ProtocolMode
import io.qpointz.mill.ai.runtime.events.routing.RoutedEventDestination
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ArtifactDescriptorRegistryTest {

    private val registry = ArtifactDescriptorRegistry.loadDefault()

    @Test
    fun shouldLoadSqlQueryDescriptors_withFullFieldSet() {
        val validation = registry.descriptorByQualifiedId("sql-query.sql-validation")
        assertNotNull(validation)
        validation!!
        assertEquals("sql-validation", validation.artifactKind)
        assertEquals("sql.validation", validation.persistKind)
        assertEquals(ArtifactSourceEvent.TOOL_RESULT, validation.sourceEvent)
        assertEquals(EmissionStrategy.FROM_TOOL_RESULT, validation.emissionStrategy)
        assertFalse(validation.persist)
        assertFalse(validation.destinations.contains(RoutedEventDestination.ARTIFACT))

        val generated = registry.descriptorByQualifiedId("sql-query.generated-sql")
        assertNotNull(generated)
        generated!!
        assertEquals("sql-query.generated-sql", generated.protocolId)
        assertEquals("generated-sql", generated.artifactKind)
        assertEquals("sql.generated", generated.persistKind)
        assertEquals(setOf("last-sql"), generated.pointerKeys)
        assertEquals("sql", generated.wirePartType)
        assertEquals("structured", generated.presentation)
        assertEquals(ProtocolMode.STRUCTURED_FINAL, generated.protocolMode)
        assertEquals(ArtifactSourceEvent.PROTOCOL_FINAL, generated.sourceEvent)
        assertEquals(EmissionStrategy.ON_TOOL_SUCCESS, generated.emissionStrategy)
        assertTrue(generated.destinations.contains(RoutedEventDestination.CHAT_STREAM))
        assertTrue(generated.destinations.contains(RoutedEventDestination.ARTIFACT))
    }

    @Test
    fun shouldLoadMetadataAuthoringInferredFacet_withOnCaptureSuccess() {
        val facet = registry.descriptorByQualifiedId("metadata-authoring.inferred-facet")
        assertNotNull(facet)
        facet!!
        assertEquals("metadata.faceting.capture", facet.protocolId)
        assertEquals("facet-proposal", facet.artifactKind)
        assertEquals("metadata.faceting.capture", facet.persistKind)
        assertEquals(setOf("metadata-facet-proposals"), facet.pointerKeys)
        assertEquals(PointerCardinality.MULTIPLE, facet.pointerCardinality)
        assertEquals("facet-proposal", facet.wirePartType)
        assertEquals(ArtifactSourceEvent.PROTOCOL_FINAL, facet.sourceEvent)
        assertEquals(EmissionStrategy.ON_CAPTURE_SUCCESS, facet.emissionStrategy)
    }

    @Test
    fun shouldResolveDistinctDescriptors_forToolResultAndProtocolFinal() {
        val fromTool = registry.descriptorForToolResultArtifactKind("sql-validation")
        val fromProtocol = registry.descriptorForProtocol("sql-query.generated-sql")
        assertNotNull(fromTool)
        assertNotNull(fromProtocol)
        assertEquals("sql.validation", fromTool!!.persistKind)
        assertEquals("sql.generated", fromProtocol!!.persistKind)
    }

    @Test
    fun shouldExposeValidateSqlEmitTrigger() {
        val triggers = registry.emitTriggersForTool("validate_sql")
        assertEquals(1, triggers.size)
        assertEquals("sql-query.generated-sql", triggers.first().artifactId)
        assertEquals("passed", triggers.first().whenField)
        assertEquals(true, triggers.first().equals)
    }

    @Test
    fun shouldFailManifestLoad_whenRequiredArtifactFieldMissing() {
        val ex = assertThrows<IllegalArgumentException> {
            CapabilityManifest.load("capabilities/bad-artifact.yaml")
        }
        assertTrue(ex.message!!.contains("sourceEvent"))
    }
}
