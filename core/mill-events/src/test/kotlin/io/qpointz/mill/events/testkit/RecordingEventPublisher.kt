package io.qpointz.mill.events.testkit

import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishOptions
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Test double that records all published events for assertion.
 */
class RecordingEventPublisher : EventPublisher {
    private val _events = CopyOnWriteArrayList<Pair<Event, PublishOptions?>>()

    /** All events published so far. */
    val events: List<Pair<Event, PublishOptions?>> get() = _events.toList()

    override fun publish(event: Event) {
        _events.add(event to null)
    }

    override fun publish(event: Event, options: PublishOptions) {
        _events.add(event to options)
    }

    /** Resets recorded state. */
    fun clear() {
        _events.clear()
    }
}
