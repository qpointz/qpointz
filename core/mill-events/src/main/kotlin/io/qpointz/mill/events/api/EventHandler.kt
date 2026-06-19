package io.qpointz.mill.events.api

import io.qpointz.mill.events.model.Event

/**
 * Functional interface for processing a single event.
 */
fun interface EventHandler {
    /**
     * Invoked when a matching event is dispatched.
     *
     * @param event the event to handle
     */
    fun onEvent(event: Event)
}
