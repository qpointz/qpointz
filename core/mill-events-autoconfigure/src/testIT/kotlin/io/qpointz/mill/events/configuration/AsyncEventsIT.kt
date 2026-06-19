package io.qpointz.mill.events.configuration

import io.qpointz.mill.events.api.EventConsumer
import io.qpointz.mill.events.api.EventPublisher
import io.qpointz.mill.events.dsl.eventConsumer
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.ProcessingMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [EventsTestApplication::class, AsyncEventsIT.AsyncTestConfig::class])
class AsyncEventsIT {

    @TestConfiguration
    class AsyncTestConfig {
        @Bean
        fun asyncLatch(): CountDownLatch = CountDownLatch(1)

        @Bean
        fun slowConsumer(asyncLatch: CountDownLatch): EventConsumer = eventConsumer {
            on(TEST_EVENT, ProcessingMode.ASYNC) { _ ->
                Thread.sleep(300)
                asyncLatch.countDown()
            }
        }
    }

    @Autowired
    lateinit var eventPublisher: EventPublisher

    @Autowired
    lateinit var asyncLatch: CountDownLatch

    @Test
    fun shouldReturnBeforeAsyncHandlerCompletes() {
        val event = Event(
            eventId = UUID.randomUUID().toString(),
            type = TEST_EVENT,
            payload = TestPayload("async-test"),
            correlationId = UUID.randomUUID().toString(),
            occurredAt = Instant.now(),
        )

        val start = System.currentTimeMillis()
        eventPublisher.publish(event)
        val elapsed = System.currentTimeMillis() - start

        assertThat(elapsed).isLessThan(200)
        assertThat(asyncLatch.await(3, TimeUnit.SECONDS)).isTrue()
    }
}
