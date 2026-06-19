package io.qpointz.mill.events.dispatch

import io.qpointz.mill.events.api.EventDispatcher
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.router.EventRouter
import java.util.concurrent.ExecutorService

/**
 * Dispatcher that honours [PublishMode] by either submitting dispatch to an executor
 * or running it inline.
 *
 * @param executor the thread pool used for [PublishMode.ASYNC] dispatch
 */
class ExecutorEventDispatcher(
    private val executor: ExecutorService,
) : EventDispatcher {

    override fun dispatch(event: Event, router: EventRouter, publishMode: PublishMode) {
        when (publishMode) {
            PublishMode.ASYNC -> executor.submit { router.dispatch(event) }
            PublishMode.SYNC -> router.dispatch(event)
        }
    }
}
