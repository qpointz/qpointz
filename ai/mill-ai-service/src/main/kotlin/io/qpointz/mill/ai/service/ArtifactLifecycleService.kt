package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.persistence.ArtifactLifecycleStatus
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.FacetArtifactProposalSupport
import io.qpointz.mill.ai.persistence.canAccept
import io.qpointz.mill.ai.persistence.canDecline
import io.qpointz.mill.ai.service.dto.ArtifactResponse
import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.metadata.events.FacetProposalRetractedPayload
import io.qpointz.mill.metadata.events.MetadataEventTypes
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.model.PublishOptions
import java.util.UUID

/**
 * Decline / accept lifecycle for facet-proposal chat artefacts.
 *
 * Captured facets are **active in rw scopes** by default. **Decline** tombstones scope rows but
 * keeps the artefact for replay. **Accept** on a declined artefact re-includes it in scopes.
 *
 * @param artifactStore durable artefact history
 * @param eventPublisher platform event bus for scope side-effects
 */
class ArtifactLifecycleService(
    private val artifactStore: ArtifactStore,
    private val eventPublisher: EventPublisher,
) {

    /**
     * Re-includes a declined facet proposal in writable scopes.
     *
     * @param chatId owning chat id
     * @param artifactId artefact primary key
     * @return updated wire artefact, or `null` when missing / not owned / not declined
     */
    fun acceptArtifact(chatId: String, artifactId: String): ArtifactResponse? {
        val record = artifactStore.findById(artifactId) ?: return null
        if (record.conversationId != chatId) return null
        if (!record.status.canAccept()) return null
        val updated = artifactStore.updateStatus(artifactId, ArtifactLifecycleStatus.ACTIVE) ?: return null
        FacetArtifactProposalSupport.publishPersistedEvent(updated, eventPublisher)
        return ArtifactWireMapper.toResponse(updated)
    }

    /**
     * Excludes a facet proposal from writable scopes (artefact remains on replay).
     *
     * @param chatId owning chat id
     * @param artifactId artefact primary key
     * @return `true` when decline was applied
     */
    fun rejectArtifact(chatId: String, artifactId: String): Boolean = declineArtifact(chatId, artifactId)

    /**
     * @see rejectArtifact
     */
    fun declineArtifact(chatId: String, artifactId: String): Boolean {
        val record = artifactStore.findById(artifactId) ?: return false
        if (record.conversationId != chatId) return false
        if (!record.status.canDecline()) return false
        artifactStore.updateStatus(artifactId, ArtifactLifecycleStatus.DECLINED)
        eventPublisher.publish(
            Event(
                eventId = UUID.randomUUID().toString(),
                type = MetadataEventTypes.FACET_PROPOSAL_RETRACTED,
                payload = FacetProposalRetractedPayload(
                    artifactId = artifactId,
                    conversationId = chatId,
                    kind = record.kind,
                ),
                correlationId = artifactId,
            ),
            PublishOptions(publishMode = PublishMode.SYNC),
        )
        return true
    }
}
