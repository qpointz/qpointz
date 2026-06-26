package io.qpointz.mill.ai.persistence

import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.metadata.events.MetadataEventTypes
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishOptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.Instant

class FacetArtifactProposalSupportTest {

    @Test
    fun shouldPublishPersistedEvent_whenWriteScopeUrnsPresent() {
        val publisher = mock<EventPublisher>()
        val artifact = ArtifactRecord(
            artifactId = "art-1",
            conversationId = "chat-1",
            runId = "run-1",
            kind = "metadata.faceting.capture",
            payload = mapOf(
                "protocolId" to "metadata.faceting.capture",
                "payload" to mapOf(
                    "facetTypeKey" to "descriptive",
                    "catalogPath" to "skymill.passenger.id",
                    "metadataEntityId" to "urn:mill/model/attribute:skymill.passenger.id",
                    "serializedPayload" to mapOf("summary" to "id"),
                    "writeScopeUrns" to listOf("urn:mill/metadata/scope:chat-chat-1"),
                ),
            ),
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        val published = FacetArtifactProposalSupport.publishPersistedEvent(artifact, publisher)

        assertThat(published).isTrue()
        val eventCaptor = argumentCaptor<Event>()
        val optionsCaptor = argumentCaptor<PublishOptions>()
        verify(publisher).publish(eventCaptor.capture(), optionsCaptor.capture())
        assertThat(eventCaptor.firstValue.type).isEqualTo(MetadataEventTypes.FACET_PROPOSAL_PERSISTED)
    }
}
