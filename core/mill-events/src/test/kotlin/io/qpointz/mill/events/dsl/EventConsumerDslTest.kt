package io.qpointz.mill.events.dsl

import io.qpointz.mill.events.model.EventType
import io.qpointz.mill.events.model.ProcessingMode
import io.qpointz.mill.events.testkit.TEST_EVENT_TYPE
import io.qpointz.mill.events.testkit.testEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList

class EventConsumerDslTest {

    @Test
    fun shouldBuildConsumerWithSingleSubscription() {
        val consumer = eventConsumer {
            on(TEST_EVENT_TYPE) { }
        }

        val subs = consumer.subscriptions()
        assertThat(subs).hasSize(1)
        assertThat(subs[0].type).isEqualTo(TEST_EVENT_TYPE)
        assertThat(subs[0].processing).isEqualTo(ProcessingMode.ASYNC)
    }

    @Test
    fun shouldBuildConsumerWithMultipleSubscriptions() {
        val typeA = EventType("type.a")
        val typeB = EventType("type.b")

        val consumer = eventConsumer {
            on(typeA, ProcessingMode.SYNC) { }
            on(typeB, ProcessingMode.AFTER_COMMIT) { }
        }

        val subs = consumer.subscriptions()
        assertThat(subs).hasSize(2)
        assertThat(subs[0].type).isEqualTo(typeA)
        assertThat(subs[0].processing).isEqualTo(ProcessingMode.SYNC)
        assertThat(subs[1].type).isEqualTo(typeB)
        assertThat(subs[1].processing).isEqualTo(ProcessingMode.AFTER_COMMIT)
    }

    @Test
    fun shouldInvokeHandlerOnEvent() {
        val received = CopyOnWriteArrayList<String>()
        val consumer = eventConsumer {
            on(TEST_EVENT_TYPE) { received.add(it.eventId) }
        }

        val event = testEvent()
        consumer.subscriptions()[0].handler.onEvent(event)

        assertThat(received).containsExactly(event.eventId)
    }
}
