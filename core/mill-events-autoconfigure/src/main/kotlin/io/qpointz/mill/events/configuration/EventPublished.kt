package io.qpointz.mill.events.configuration

import io.qpointz.mill.events.model.Event
import org.springframework.context.ApplicationEvent

/**
 * Spring [ApplicationEvent] wrapper that carries a Mill [Event] through the
 * Spring event infrastructure.
 *
 * @property event the wrapped Mill event
 */
class EventPublished(source: Any, val event: Event) : ApplicationEvent(source)
