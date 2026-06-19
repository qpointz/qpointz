package io.qpointz.mill.events.configuration

import io.qpointz.mill.events.api.EventDispatcher
import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.events.api.EventTransport
import io.qpointz.mill.events.dispatch.DirectEventDispatcher
import io.qpointz.mill.events.dispatch.ExecutorEventDispatcher
import io.qpointz.mill.events.router.EventRouter
import io.qpointz.mill.events.transport.InMemoryEventTransport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class EventsAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(EventsAutoConfiguration::class.java))

    @Test
    fun shouldRegisterAllBeansWithDefaults() {
        contextRunner.run { context ->
            assertThat(context).hasSingleBean(EventRouter::class.java)
            assertThat(context).hasSingleBean(EventDispatcher::class.java)
            assertThat(context).hasSingleBean(EventTransport::class.java)
            assertThat(context).hasSingleBean(EventPublisher::class.java)
        }
    }

    @Test
    fun shouldUseInMemoryTransportByDefault() {
        contextRunner.run { context ->
            assertThat(context.getBean(EventTransport::class.java))
                .isInstanceOf(InMemoryEventTransport::class.java)
        }
    }

    @Test
    fun shouldUseExecutorDispatcherWhenAsyncEnabled() {
        contextRunner.run { context ->
            assertThat(context.getBean(EventDispatcher::class.java))
                .isInstanceOf(ExecutorEventDispatcher::class.java)
        }
    }

    @Test
    fun shouldUseDirectDispatcherWhenAsyncDisabled() {
        contextRunner
            .withPropertyValues("mill.events.async.enabled=false")
            .run { context ->
                assertThat(context.getBean(EventDispatcher::class.java))
                    .isInstanceOf(DirectEventDispatcher::class.java)
            }
    }

    @Test
    fun shouldUseSpringTransportWhenConfigured() {
        contextRunner
            .withPropertyValues("mill.events.transport=spring")
            .run { context ->
                assertThat(context.getBean(EventTransport::class.java))
                    .isInstanceOf(SpringEventTransport::class.java)
            }
    }

    @Test
    fun shouldRegisterSpringListenerForSpringTransport() {
        contextRunner
            .withPropertyValues("mill.events.transport=spring")
            .run { context ->
                assertThat(context).hasSingleBean(EventSpringListener::class.java)
            }
    }

    @Test
    fun shouldNotRegisterSpringListenerForInMemoryTransport() {
        contextRunner.run { context ->
            assertThat(context).doesNotHaveBean(EventSpringListener::class.java)
        }
    }
}
