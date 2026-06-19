package io.qpointz.mill.events.api

/**
 * Declares one or more event subscriptions to be registered with the router.
 *
 * Implementations are typically created via [io.qpointz.mill.events.dsl.eventConsumer] DSL
 * and exposed as Spring beans.
 */
interface EventConsumer {
    /**
     * Returns the list of subscriptions this consumer contributes to the router.
     */
    fun subscriptions(): List<EventSubscription>
}
