package io.qpointz.mill.events.publisher

import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.events.api.EventTransport
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishOptions

/**
 * Default [EventPublisher] implementation that delegates to the configured [EventTransport].
 *
 * @param transport the transport layer for event delivery
 * @param defaultOptions fallback options when none are provided per-call
 */
class DefaultEventPublisher(
    private val transport: EventTransport,
    private val defaultOptions: PublishOptions = PublishOptions(),
) : EventPublisher {

    override fun publish(event: Event) {
        transport.publish(event, defaultOptions)
    }

    override fun publish(event: Event, options: PublishOptions) {
        transport.publish(event, options)
    }
}
