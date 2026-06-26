package io.qpointz.mill.ai.persistence

import io.qpointz.mill.ai.core.artifact.FacetProposalWire
import io.qpointz.mill.metadata.events.FacetProposalPersistedPayload
import io.qpointz.mill.metadata.events.MetadataEventTypes
import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.model.PublishOptions
import java.util.UUID

/**
 * Shared extraction and event publish for facet-proposal chat artefacts (WI-360).
 */
object FacetArtifactProposalSupport {

    /**
     * Parsed facet proposal fields from a persisted artefact body.
     */
    data class Extracted(
        val facetTypeKey: String,
        val metadataEntityId: String,
        val payload: Map<String, Any?>,
        val writeScopeUrns: List<String>,
    )

    /**
     * @param artifact persisted chat artefact row
     * @return extracted proposal when this artefact is a facet capture with a target entity
     */
    fun extract(artifact: ArtifactRecord): Extracted? {
        if (!artifact.status.isIncludedInScopes()) return null
        val inner = extractInnerPayload(artifact.payload) ?: return null
        val wire = FacetProposalWire.normalizePayload(inner) ?: return null
        val facetTypeKey = wire["facetTypeKey"] as? String ?: return null
        val metadataEntityId = wire["metadataEntityId"] as? String ?: return null
        val payload = (wire["payload"] as? Map<String, Any?>) ?: emptyMap()
        val writeScopeUrns = (inner["writeScopeUrns"] as? List<*>)?.mapNotNull { it as? String }
            ?: emptyList()
        return Extracted(
            facetTypeKey = facetTypeKey,
            metadataEntityId = metadataEntityId,
            payload = payload,
            writeScopeUrns = writeScopeUrns,
        )
    }

    /**
     * Publishes [MetadataEventTypes.FACET_PROPOSAL_PERSISTED] when [artifact] is a facet proposal with write scopes.
     *
     * @param artifact persisted chat artefact
     * @param eventPublisher platform bus
     * @return `true` when an event was published
     */
    fun publishPersistedEvent(artifact: ArtifactRecord, eventPublisher: EventPublisher): Boolean {
        val proposal = extract(artifact) ?: return false
        if (proposal.writeScopeUrns.isEmpty()) return false
        eventPublisher.publish(
            Event(
                eventId = UUID.randomUUID().toString(),
                type = MetadataEventTypes.FACET_PROPOSAL_PERSISTED,
                payload = FacetProposalPersistedPayload(
                    artifactId = artifact.artifactId,
                    conversationId = artifact.conversationId,
                    kind = artifact.kind,
                    facetTypeKey = proposal.facetTypeKey,
                    metadataEntityId = proposal.metadataEntityId,
                    payload = proposal.payload,
                    writeScopeUrns = proposal.writeScopeUrns,
                ),
                correlationId = artifact.artifactId,
            ),
            PublishOptions(publishMode = PublishMode.SYNC),
        )
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractInnerPayload(content: Map<String, Any?>): Map<String, Any?>? {
        var current: Map<String, Any?> = content
        repeat(4) {
            if (isRecognized(current)) return current
            val nested = current["payload"]
            if (nested is Map<*, *>) {
                current = nested as Map<String, Any?>
            } else {
                return current
            }
        }
        return current
    }

    private fun isRecognized(map: Map<String, Any?>): Boolean =
        map["facetTypeKey"] is String ||
            map["metadataEntityId"] is String ||
            map["writeScopeUrns"] is List<*>
}
