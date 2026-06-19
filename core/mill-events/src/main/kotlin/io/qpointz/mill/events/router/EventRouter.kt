package io.qpointz.mill.events.router

import io.qpointz.mill.events.api.EventSubscription
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.ProcessingMode
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService

/**
 * Multicast event router that dispatches events to matching subscriptions by [Event.type] id.
 *
 * Failures in individual handlers are isolated: logged and skipped without affecting other
 * subscriptions on the same event type.
 *
 * @param subscriptions all subscriptions to index by event type id
 * @param asyncExecutor executor for [ProcessingMode.ASYNC] handler invocations
 */
class EventRouter(
    subscriptions: List<EventSubscription>,
    private val asyncExecutor: ExecutorService? = null,
) {
    private val log = LoggerFactory.getLogger(EventRouter::class.java)
    private val index: Map<String, List<EventSubscription>> =
        subscriptions.groupBy { it.type.id }

    /**
     * Dispatches an event to all handlers subscribed to its [Event.type] id.
     *
     * [ProcessingMode.SYNC] and [ProcessingMode.AFTER_COMMIT] handlers run on the calling thread.
     * [ProcessingMode.ASYNC] handlers are submitted to the executor if available, otherwise
     * run inline.
     *
     * @param event the event to dispatch
     */
    fun dispatch(event: Event) {
        val handlers = index[event.type.id] ?: return
        for (sub in handlers) {
            when (sub.processing) {
                ProcessingMode.SYNC, ProcessingMode.AFTER_COMMIT -> invokeSync(sub, event)
                ProcessingMode.ASYNC -> invokeAsync(sub, event)
            }
        }
    }

    private fun invokeSync(sub: EventSubscription, event: Event) {
        try {
            sub.handler.onEvent(event)
        } catch (e: Exception) {
            log.error("Handler failed for event type '{}', eventId='{}': {}",
                event.type.id, event.eventId, e.message, e)
        }
    }

    private fun invokeAsync(sub: EventSubscription, event: Event) {
        val executor = asyncExecutor
        if (executor != null) {
            executor.submit { invokeSync(sub, event) }
        } else {
            invokeSync(sub, event)
        }
    }
}
