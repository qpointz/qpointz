package io.qpointz.mill.ai.persistence

import io.qpointz.mill.events.api.EventPublisher

/**
 * Publishes [io.qpointz.mill.metadata.events.MetadataEventTypes.FACET_PROPOSAL_PERSISTED] after facet-proposal artefacts are saved.
 */
class FacetArtifactEventPublisher(
    private val eventPublisher: EventPublisher,
) : ArtifactObserver {

    override fun onArtifactCreated(artifact: ArtifactRecord) {
        FacetArtifactProposalSupport.publishPersistedEvent(artifact, eventPublisher)
    }
}
