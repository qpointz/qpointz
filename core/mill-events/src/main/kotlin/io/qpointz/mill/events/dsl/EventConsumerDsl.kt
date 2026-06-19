package io.qpointz.mill.events.dsl

import io.qpointz.mill.events.api.EventConsumer
import io.qpointz.mill.events.api.EventHandler
import io.qpointz.mill.events.api.EventSubscription
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.EventType
import io.qpointz.mill.events.model.ProcessingMode

/**
 * DSL builder for declaring event subscriptions in a concise block syntax.
 *
 * Usage:
 * ```kotlin
 * val consumer = eventConsumer {
 *     on(EventTypes.METADATA_ENTITY_UPDATED, ProcessingMode.SYNC) { event -> handle(event) }
 * }
 * ```
 */
class EventConsumerBuilder {
    private val subscriptions = mutableListOf<EventSubscription>()

    /**
     * Registers a subscription for the given event type.
     *
     * @param type the event type to subscribe to
     * @param processing the processing mode for this handler (defaults to [ProcessingMode.ASYNC])
     * @param handler the callback invoked on matching events
     */
    fun on(type: EventType, processing: ProcessingMode = ProcessingMode.ASYNC, handler: (Event) -> Unit) {
        subscriptions.add(EventSubscription(type, EventHandler { handler(it) }, processing))
    }

    internal fun build(): EventConsumer = DslEventConsumer(subscriptions.toList())
}

/**
 * Creates an [EventConsumer] from a DSL block.
 *
 * @param block builder block declaring subscriptions via [EventConsumerBuilder.on]
 * @return a consumer ready for registration with the event router
 */
fun eventConsumer(block: EventConsumerBuilder.() -> Unit): EventConsumer {
    return EventConsumerBuilder().apply(block).build()
}

private class DslEventConsumer(
    private val subs: List<EventSubscription>,
) : EventConsumer {
    override fun subscriptions(): List<EventSubscription> = subs
}
