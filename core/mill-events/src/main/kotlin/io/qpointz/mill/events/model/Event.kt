package io.qpointz.mill.events.model

import java.time.Instant

/**
 * Immutable envelope for an application event.
 *
 * @property eventId unique identifier for this event instance
 * @property type routing key determining which subscribers receive the event
 * @property payload domain-specific data carried by the event
 * @property correlationId logical correlation across related events (e.g. request id)
 * @property partitionKey optional key for ordered delivery in distributed transports
 * @property occurredAt timestamp when the event was produced
 * @property schemaVersion payload schema version for forward compatibility
 */
data class Event(
    val eventId: String,
    val type: EventType,
    val payload: EventPayload,
    val correlationId: String,
    val partitionKey: String? = null,
    val occurredAt: Instant = Instant.now(),
    val schemaVersion: Int = 1,
)
