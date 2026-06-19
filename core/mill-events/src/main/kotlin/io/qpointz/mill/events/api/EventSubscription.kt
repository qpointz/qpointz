package io.qpointz.mill.events.api

import io.qpointz.mill.events.model.EventType
import io.qpointz.mill.events.model.ProcessingMode

/**
 * Binds a handler to a specific event type with a processing mode.
 *
 * @property type the event type this subscription listens for
 * @property handler the callback invoked on matching events
 * @property processing controls execution semantics (async, sync, after-commit)
 */
data class EventSubscription(
    val type: EventType,
    val handler: EventHandler,
    val processing: ProcessingMode = ProcessingMode.ASYNC,
)
