package io.qpointz.mill.events.configuration

import io.qpointz.mill.events.api.EventDispatcher
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.router.EventRouter
import org.springframework.context.event.EventListener

/**
 * Receives [EventPublished] wrappers from the Spring event bus and routes them
 * through the Mill [EventDispatcher] and [EventRouter].
 *
 * @param dispatcher the dispatcher applying publish mode semantics
 * @param router the multicast router
 */
class EventSpringListener(
    private val dispatcher: EventDispatcher,
    private val router: EventRouter,
) {

    /**
     * Handles Spring-published Mill events by dispatching them through the router.
     *
     * Spring transport always uses [PublishMode.SYNC] dispatch since the Spring event
     * infrastructure already handles the async boundary.
     */
    @EventListener
    fun onEvent(published: EventPublished) {
        dispatcher.dispatch(published.event, router, PublishMode.SYNC)
    }
}
