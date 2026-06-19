package io.qpointz.mill.events.testkit

import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.EventType
import java.time.Instant
import java.util.UUID

/** Test-only event type — not part of the production catalog. */
val TEST_EVENT_TYPE = EventType("mill.test.event")

/** Creates a test event with sensible defaults. */
fun testEvent(
    type: EventType = TEST_EVENT_TYPE,
    message: String = "test",
    correlationId: String = UUID.randomUUID().toString(),
): Event = Event(
    eventId = UUID.randomUUID().toString(),
    type = type,
    payload = TestEventPayload(message),
    correlationId = correlationId,
    occurredAt = Instant.now(),
)
