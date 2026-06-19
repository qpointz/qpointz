package io.qpointz.mill.events.transport

import io.qpointz.mill.events.api.EventDispatcher
import io.qpointz.mill.events.api.EventTransport
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishOptions
import io.qpointz.mill.events.router.EventRouter

/**
 * In-memory transport that delegates directly to the dispatcher and router.
 *
 * Suitable for single-JVM deployments and tests. No serialization, no broker.
 *
 * @param dispatcher controls async/sync dispatch semantics
 * @param router the event router holding subscription index
 */
class InMemoryEventTransport(
    private val dispatcher: EventDispatcher,
    private val router: EventRouter,
) : EventTransport {

    override fun publish(event: Event, options: PublishOptions) {
        dispatcher.dispatch(event, router, options.publishMode)
    }
}
