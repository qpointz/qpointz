package io.qpointz.mill.events.api

import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishOptions

/**
 * Primary entry point for publishing events into the event bus.
 *
 * Producers depend on this interface; the underlying transport is transparent.
 */
interface EventPublisher {
    /**
     * Publishes an event using the configured default options.
     *
     * @param event the event to publish
     */
    fun publish(event: Event)

    /**
     * Publishes an event with explicit per-call options.
     *
     * @param event the event to publish
     * @param options overrides for this specific publish call
     */
    fun publish(event: Event, options: PublishOptions)
}
