package io.qpointz.mill.metadata.events

import io.qpointz.mill.events.model.EventType

/**
 * Metadata domain event type constants.
 *
 * Wire keys are stable contracts for producers (e.g. chat artefact capture) and consumers
 * (scope assignment, search bridges). Prefer these over duplicating strings in other modules.
 */
object MetadataEventTypes {

    /**
     * Fired after a facet-proposal artefact is durably saved with non-empty writable scopes.
     *
     * Consumers materialize rows in [io.qpointz.mill.metadata.domain.facet.FacetAssignment] per scope.
     */
    val FACET_PROPOSAL_PERSISTED = EventType("artifact.facet.persisted")

    /**
     * Fired when a facet-proposal artefact is retracted (Reject or equivalent lifecycle).
     *
     * Wire key matches platform [io.qpointz.mill.events.catalog.EventTypes.ARTIFACT_RETRACTED] so
     * artefact and metadata handlers can share the same bus routing key.
     */
    val FACET_PROPOSAL_RETRACTED = EventType("artifact.retracted")
}
