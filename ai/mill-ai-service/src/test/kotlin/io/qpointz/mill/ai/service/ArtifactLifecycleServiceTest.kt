package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.persistence.ArtifactLifecycleStatus
import io.qpointz.mill.ai.persistence.ArtifactRecord
import io.qpointz.mill.ai.persistence.InMemoryArtifactStore
import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.model.PublishOptions
import io.qpointz.mill.metadata.events.FacetProposalRetractedPayload
import io.qpointz.mill.metadata.events.MetadataEventTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList

class ArtifactLifecycleServiceTest {

    private val store = InMemoryArtifactStore()
    private val published = CopyOnWriteArrayList<Event>()

    private val publisher = object : EventPublisher {
        override fun publish(event: Event) {
            published.add(event)
        }

        override fun publish(event: Event, options: PublishOptions) {
            published.add(event)
            assertThat(options.publishMode).isEqualTo(PublishMode.SYNC)
        }
    }

    private val service = ArtifactLifecycleService(store, publisher)

    private fun facetRecord(
        artifactId: String = "art-1",
        chatId: String = "chat-1",
        status: ArtifactLifecycleStatus = ArtifactLifecycleStatus.ACTIVE,
    ) = ArtifactRecord(
        artifactId = artifactId,
        conversationId = chatId,
        runId = "run-1",
        kind = "metadata.faceting.capture",
        payload = mapOf(
            "payload" to mapOf(
                "facetTypeKey" to "descriptive",
                "metadataEntityId" to "urn:mill/model/table:sales.customers",
                "serializedPayload" to mapOf("summary" to "VIP"),
                "writeScopeUrns" to listOf("urn:mill/metadata/scope:chat-chat-1"),
            ),
        ),
        turnId = "turn-1",
        status = status,
        createdAt = Instant.parse("2025-01-01T00:00:00Z"),
    )

    @Test
    fun shouldReincludeDeclinedFacet_onAccept() {
        store.save(facetRecord(status = ArtifactLifecycleStatus.DECLINED))

        val response = service.acceptArtifact("chat-1", "art-1")

        assertThat(response).isNotNull
        assertThat(response!!.status).isEqualTo("active")
        assertThat(store.findById("art-1")!!.status).isEqualTo(ArtifactLifecycleStatus.ACTIVE)
        assertThat(published).hasSize(1)
        assertThat(published.single().type).isEqualTo(MetadataEventTypes.FACET_PROPOSAL_PERSISTED)
    }

    @Test
    fun shouldExcludeActiveFacet_onDecline_andKeepArtifact() {
        store.save(facetRecord())

        val ok = service.declineArtifact("chat-1", "art-1")

        assertThat(ok).isTrue()
        assertThat(store.findById("art-1")).isNotNull
        assertThat(store.findById("art-1")!!.status).isEqualTo(ArtifactLifecycleStatus.DECLINED)
        assertThat(published).hasSize(1)
        val event = published.single()
        assertThat(event.type).isEqualTo(MetadataEventTypes.FACET_PROPOSAL_RETRACTED)
        val payload = event.payload as FacetProposalRetractedPayload
        assertThat(payload.artifactId).isEqualTo("art-1")
        assertThat(payload.conversationId).isEqualTo("chat-1")
    }

    @Test
    fun shouldReturnNull_whenAcceptingActiveFacet() {
        store.save(facetRecord())

        assertThat(service.acceptArtifact("chat-1", "art-1")).isNull()
    }

    @Test
    fun shouldReturnFalse_whenDecliningAlreadyDeclinedFacet() {
        store.save(facetRecord(status = ArtifactLifecycleStatus.DECLINED))

        assertThat(service.declineArtifact("chat-1", "art-1")).isFalse()
    }

    @Test
    fun shouldReturnNull_whenAcceptingWrongChat() {
        store.save(facetRecord(status = ArtifactLifecycleStatus.DECLINED))

        assertThat(service.acceptArtifact("other-chat", "art-1")).isNull()
    }
}
