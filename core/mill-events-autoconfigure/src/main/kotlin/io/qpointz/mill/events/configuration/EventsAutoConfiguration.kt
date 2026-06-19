package io.qpointz.mill.events.configuration

import io.qpointz.mill.events.api.EventConsumer
import io.qpointz.mill.events.api.EventDispatcher
import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.events.api.EventTransport
import io.qpointz.mill.events.dispatch.DirectEventDispatcher
import io.qpointz.mill.events.dispatch.ExecutorEventDispatcher
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.model.PublishOptions
import io.qpointz.mill.events.publisher.DefaultEventPublisher
import io.qpointz.mill.events.router.EventRouter
import io.qpointz.mill.events.transport.InMemoryEventTransport
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Auto-configuration for the Mill event bus.
 *
 * Registers router, dispatcher, transport, and publisher beans based on
 * [EventsProperties] (`mill.events.*`).
 */
@AutoConfiguration
@EnableConfigurationProperties(EventsProperties::class)
class EventsAutoConfiguration {

    /**
     * Shared executor for async dispatch and async handler processing.
     */
    @Bean
    @ConditionalOnMissingBean(name = ["eventBusExecutor"])
    fun eventBusExecutor(properties: EventsProperties): ExecutorService {
        return if (properties.async.isEnabled) {
            Executors.newCachedThreadPool { r ->
                Thread(r, "mill-events-worker").apply { isDaemon = true }
            }
        } else {
            Executors.newSingleThreadExecutor()
        }
    }

    /**
     * Builds the event router from all [EventConsumer] beans in the context.
     */
    @Bean
    @ConditionalOnMissingBean
    fun eventRouter(
        consumers: ObjectProvider<EventConsumer>,
        eventBusExecutor: ExecutorService,
        properties: EventsProperties,
    ): EventRouter {
        val subscriptions = consumers.orderedStream()
            .flatMap { it.subscriptions().stream() }
            .toList()
        val executor = if (properties.async.isEnabled) eventBusExecutor else null
        return EventRouter(subscriptions, asyncExecutor = executor)
    }

    /**
     * Selects the dispatcher implementation based on async configuration.
     */
    @Bean
    @ConditionalOnMissingBean
    fun eventDispatcher(
        eventBusExecutor: ExecutorService,
        properties: EventsProperties,
    ): EventDispatcher {
        return if (properties.async.isEnabled) {
            ExecutorEventDispatcher(eventBusExecutor)
        } else {
            DirectEventDispatcher()
        }
    }

    /**
     * Selects the transport implementation based on [EventsProperties.getTransport].
     */
    @Bean
    @ConditionalOnMissingBean
    fun eventTransport(
        dispatcher: EventDispatcher,
        router: EventRouter,
        properties: EventsProperties,
        applicationEventPublisher: ObjectProvider<ApplicationEventPublisher>,
    ): EventTransport {
        return when (properties.transport) {
            "spring" -> SpringEventTransport(
                applicationEventPublisher.getObject()
            )
            else -> InMemoryEventTransport(dispatcher, router)
        }
    }

    /**
     * Registers the Spring event listener when Spring transport is selected.
     */
    @Bean
    @ConditionalOnMissingBean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        prefix = "mill.events", name = ["transport"], havingValue = "spring"
    )
    fun eventSpringListener(
        dispatcher: EventDispatcher,
        router: EventRouter,
    ): EventSpringListener {
        return EventSpringListener(dispatcher, router)
    }

    /**
     * Default event publisher backed by the selected transport.
     */
    @Bean
    @ConditionalOnMissingBean
    fun eventPublisher(
        transport: EventTransport,
        properties: EventsProperties,
    ): EventPublisher {
        val defaultMode = when (properties.publish.mode) {
            "sync" -> PublishMode.SYNC
            else -> PublishMode.ASYNC
        }
        return DefaultEventPublisher(transport, PublishOptions(publishMode = defaultMode))
    }
}
