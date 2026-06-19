package io.qpointz.mill.events.configuration

import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.EventType
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.model.PublishOptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.time.Instant
import java.util.UUID

@SpringBootTest(classes = [EventsTestApplication::class, StubEventConsumersConfiguration::class])
@TestPropertySource(properties = ["mill.events.async.enabled=false"])
class EventsAutoConfigurationIT {

    @Autowired
    lateinit var eventPublisher: EventPublisher

    @Autowired
    lateinit var recorder: EventRecorder

    @BeforeEach
    fun setUp() {
        recorder.clear()
    }

    private fun publishTestEvent(message: String = "hello"): Event {
        val event = Event(
            eventId = UUID.randomUUID().toString(),
            type = TEST_EVENT,
            payload = TestPayload(message),
            correlationId = UUID.randomUUID().toString(),
            occurredAt = Instant.now(),
        )
        eventPublisher.publish(event, PublishOptions(PublishMode.SYNC))
        return event
    }

    @Test
    fun shouldFanOutToMultipleConsumers() {
        val event = publishTestEvent()

        val tags = recorder.entries.map { it.first }
        assertThat(tags).contains("A", "B")
        assertThat(recorder.entries.filter { it.first == "A" }).hasSize(1)
        assertThat(recorder.entries.filter { it.first == "B" }).hasSize(1)
    }

    @Test
    fun shouldIsolateFailingConsumer() {
        publishTestEvent()

        val tags = recorder.entries.map { it.first }
        assertThat(tags).contains("A", "B")
    }

    @Test
    fun shouldNotDeliverToUnsubscribedType() {
        val event = Event(
            eventId = UUID.randomUUID().toString(),
            type = EventType("unmatched.type"),
            payload = TestPayload("nope"),
            correlationId = UUID.randomUUID().toString(),
            occurredAt = Instant.now(),
        )
        eventPublisher.publish(event, PublishOptions(PublishMode.SYNC))

        assertThat(recorder.entries).isEmpty()
    }
}
