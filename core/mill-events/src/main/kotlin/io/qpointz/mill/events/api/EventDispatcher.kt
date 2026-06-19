package io.qpointz.mill.events.api

import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.router.EventRouter

/**
 * Bridges between transport and router, applying [PublishMode] semantics.
 *
 * The dispatcher decides whether to invoke the router synchronously on the caller thread
 * or asynchronously on a separate executor.
 */
interface EventDispatcher {
    /**
     * Dispatches an event through the given router.
     *
     * @param event the event to dispatch
     * @param router the router holding subscription index
     * @param publishMode controls whether dispatch is synchronous or asynchronous
     */
    fun dispatch(event: Event, router: EventRouter, publishMode: PublishMode)
}
