package io.qpointz.mill.events.configuration

import io.qpointz.mill.events.api.EventConsumer
import io.qpointz.mill.events.dsl.eventConsumer
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.EventType
import io.qpointz.mill.events.model.ProcessingMode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.CopyOnWriteArrayList

/** Test-only event type — not part of production catalog. */
val TEST_EVENT = EventType("mill.test.event")

/**
 * Shared recorder for verifying event delivery in integration tests.
 */
class EventRecorder {
    private val _entries = CopyOnWriteArrayList<Pair<String, Event>>()

    /** All recorded entries as (tag, event) pairs. */
    val entries: List<Pair<String, Event>> get() = _entries.toList()

    /** Records an event with a tag identifying the consumer. */
    fun record(tag: String, event: Event) {
        _entries.add(tag to event)
    }

    /** Resets recorded state. */
    fun clear() {
        _entries.clear()
    }
}

/**
 * Test stub consumers for integration testing fan-out, failure isolation, and async behaviour.
 */
@Configuration
class StubEventConsumersConfiguration {

    @Bean
    fun eventRecorder(): EventRecorder = EventRecorder()

    @Bean
    fun stubConsumerA(recorder: EventRecorder): EventConsumer = eventConsumer {
        on(TEST_EVENT, ProcessingMode.SYNC) { event ->
            recorder.record("A", event)
        }
    }

    @Bean
    fun stubConsumerB(recorder: EventRecorder): EventConsumer = eventConsumer {
        on(TEST_EVENT, ProcessingMode.SYNC) { event ->
            recorder.record("B", event)
        }
    }

    @Bean
    fun failingConsumer(): EventConsumer = eventConsumer {
        on(TEST_EVENT, ProcessingMode.SYNC) { _ ->
            throw RuntimeException("Intentional test failure")
        }
    }
}
