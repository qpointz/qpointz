package io.qpointz.mill.metadata.events

import io.qpointz.mill.events.model.EventPayload

/**
 * Payload for [MetadataEventTypes.FACET_PROPOSAL_PERSISTED].
 *
 * @property artifactId persisted chat artefact id used as [io.qpointz.mill.metadata.domain.facet.FacetAssignment.sourceArtifactId]
 * @property conversationId owning chat id
 * @property kind persisted artefact kind (e.g. `metadata.faceting.capture`)
 * @property facetTypeKey target facet type key or URN
 * @property metadataEntityId target entity URN
 * @property payload facet JSON body
 * @property writeScopeUrns scopes to materialize into
 */
data class FacetProposalPersistedPayload(
    val artifactId: String,
    val conversationId: String,
    val kind: String,
    val facetTypeKey: String,
    val metadataEntityId: String,
    val payload: Map<String, Any?>,
    val writeScopeUrns: List<String>,
) : EventPayload

/**
 * Payload for [MetadataEventTypes.FACET_PROPOSAL_RETRACTED].
 *
 * @property artifactId retracted artefact id
 * @property conversationId owning chat id
 * @property kind artefact kind for handler routing
 */
data class FacetProposalRetractedPayload(
    val artifactId: String,
    val conversationId: String,
    val kind: String,
) : EventPayload
