package io.qpointz.mill.events.catalog

import io.qpointz.mill.events.model.EventType

/**
 * Reserved event type constants for the Mill platform.
 *
 * These define the routing keys; actual producers and consumers are implemented
 * in domain-specific modules (follow-on stories).
 */
object EventTypes {
    /** Fired when a metadata entity is created. */
    val METADATA_ENTITY_CREATED = EventType("metadata.entity.created")

    /** Fired when a metadata entity is updated. */
    val METADATA_ENTITY_UPDATED = EventType("metadata.entity.updated")

    /** Fired when a metadata entity is deleted. */
    val METADATA_ENTITY_DELETED = EventType("metadata.entity.deleted")

    /** Fired when a metadata facet is updated. */
    val METADATA_FACET_UPDATED = EventType("metadata.facet.updated")

    /** Fired when a SQL artifact is persisted. */
    val ARTIFACT_SQL_PERSISTED = EventType("artifact.sql.persisted")

    /** Fired when a chat turn completes. */
    val CHAT_TURN_COMPLETED = EventType("chat.turn.completed")

    /**
     * Fired when an artefact is retracted (Reject or future delete APIs).
     *
     * Facet proposal producers and payloads are defined in
     * [io.qpointz.mill.metadata.events.MetadataEventTypes] and
     * [io.qpointz.mill.metadata.events.FacetProposalRetractedPayload].
     */
    val ARTIFACT_RETRACTED = EventType("artifact.retracted")
}
