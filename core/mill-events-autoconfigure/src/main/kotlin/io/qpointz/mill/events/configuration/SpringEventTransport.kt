package io.qpointz.mill.events.configuration

import io.qpointz.mill.events.api.EventTransport
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishOptions
import org.springframework.context.ApplicationEventPublisher

/**
 * Transport that bridges Mill events into the Spring [ApplicationEventPublisher].
 *
 * The Spring event listener ([EventSpringListener]) receives the wrapper and routes
 * through the dispatcher/router.
 *
 * @param applicationEventPublisher the Spring publisher to delegate to
 */
class SpringEventTransport(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : EventTransport {

    override fun publish(event: Event, options: PublishOptions) {
        applicationEventPublisher.publishEvent(EventPublished(this, event))
    }
}
