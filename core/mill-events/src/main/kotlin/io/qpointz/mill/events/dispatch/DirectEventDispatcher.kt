package io.qpointz.mill.events.dispatch

import io.qpointz.mill.events.api.EventDispatcher
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.router.EventRouter

/**
 * Dispatcher that always invokes the router inline on the calling thread,
 * ignoring [PublishMode].
 *
 * Intended for unit tests where deterministic, single-threaded execution is required.
 */
class DirectEventDispatcher : EventDispatcher {
    override fun dispatch(event: Event, router: EventRouter, publishMode: PublishMode) {
        router.dispatch(event)
    }
}
