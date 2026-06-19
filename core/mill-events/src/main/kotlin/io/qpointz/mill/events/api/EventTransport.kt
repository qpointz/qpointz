package io.qpointz.mill.events.api

import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishOptions

/**
 * Messaging-plane adapter that delivers events to the dispatch layer.
 *
 * Implementations are broker-aware (in-memory, Spring, Kafka); producers and consumers
 * never depend on a specific transport directly.
 */
interface EventTransport {
    /**
     * Accepts an event for delivery through this transport.
     *
     * @param event the event to deliver
     * @param options publish options controlling delivery semantics
     */
    fun publish(event: Event, options: PublishOptions = PublishOptions())
}
