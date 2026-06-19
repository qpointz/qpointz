package io.qpointz.mill.events.publisher

import io.qpointz.mill.events.api.EventTransport
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.model.PublishOptions
import io.qpointz.mill.events.testkit.testEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList

class DefaultEventPublisherTest {

    private fun recordingTransport(calls: MutableList<Pair<Event, PublishOptions>>): EventTransport {
        return object : EventTransport {
            override fun publish(event: Event, options: PublishOptions) {
                calls.add(event to options)
            }
        }
    }

    @Test
    fun shouldDelegateToTransportWithDefaultOptions() {
        val calls = CopyOnWriteArrayList<Pair<Event, PublishOptions>>()
        val transport = recordingTransport(calls)
        val publisher = DefaultEventPublisher(transport)

        val event = testEvent()
        publisher.publish(event)

        assertThat(calls).hasSize(1)
        assertThat(calls[0].first.eventId).isEqualTo(event.eventId)
        assertThat(calls[0].second.publishMode).isEqualTo(PublishMode.ASYNC)
    }

    @Test
    fun shouldDelegateToTransportWithExplicitOptions() {
        val calls = CopyOnWriteArrayList<Pair<Event, PublishOptions>>()
        val transport = recordingTransport(calls)
        val publisher = DefaultEventPublisher(transport)

        val event = testEvent()
        val opts = PublishOptions(publishMode = PublishMode.SYNC)
        publisher.publish(event, opts)

        assertThat(calls).hasSize(1)
        assertThat(calls[0].second.publishMode).isEqualTo(PublishMode.SYNC)
    }

    @Test
    fun shouldUseCustomDefaultOptions() {
        val calls = CopyOnWriteArrayList<Pair<Event, PublishOptions>>()
        val transport = recordingTransport(calls)
        val defaultOpts = PublishOptions(publishMode = PublishMode.SYNC)
        val publisher = DefaultEventPublisher(transport, defaultOpts)

        publisher.publish(testEvent())

        assertThat(calls[0].second.publishMode).isEqualTo(PublishMode.SYNC)
    }
}
