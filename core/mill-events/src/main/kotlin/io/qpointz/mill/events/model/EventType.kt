package io.qpointz.mill.events.model

/**
 * Identifies a category of domain event used as the routing key for dispatch.
 *
 * @property id dot-separated identifier (e.g. `metadata.entity.created`)
 */
data class EventType(val id: String)
