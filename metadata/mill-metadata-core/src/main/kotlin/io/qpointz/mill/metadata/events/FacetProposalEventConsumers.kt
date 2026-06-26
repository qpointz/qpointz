package io.qpointz.mill.metadata.events

import io.qpointz.mill.events.api.EventConsumer
import io.qpointz.mill.events.dsl.eventConsumer
import io.qpointz.mill.events.model.ProcessingMode
import io.qpointz.mill.metadata.service.facet.FacetArtifactScopeService

/**
 * Registers mill-events consumers that apply facet proposal lifecycle to metadata scopes.
 *
 * @param scopeService assigns or tombstones scope rows from chat artefact proposals
 */
class FacetProposalEventConsumers(
    private val scopeService: FacetArtifactScopeService,
) {

    /** Event bus consumer wiring facet proposal persist/retract handlers. */
    fun consumer(): EventConsumer = eventConsumer {
        on(MetadataEventTypes.FACET_PROPOSAL_PERSISTED, ProcessingMode.SYNC) { event ->
            val payload = event.payload as? FacetProposalPersistedPayload ?: return@on
            scopeService.assignFromProposal(
                sourceArtifactId = payload.artifactId,
                metadataEntityId = payload.metadataEntityId,
                facetTypeKey = payload.facetTypeKey,
                payload = payload.payload,
                writeScopeUrns = payload.writeScopeUrns,
            )
        }
        on(MetadataEventTypes.FACET_PROPOSAL_RETRACTED, ProcessingMode.SYNC) { event ->
            val payload = event.payload as? FacetProposalRetractedPayload ?: return@on
            if (!isFacetProposalKind(payload.kind)) return@on
            scopeService.retractBySourceArtifactId(payload.artifactId)
        }
    }

    private fun isFacetProposalKind(kind: String): Boolean =
        kind.contains("faceting", ignoreCase = true) || kind.contains("facet", ignoreCase = true)
}
