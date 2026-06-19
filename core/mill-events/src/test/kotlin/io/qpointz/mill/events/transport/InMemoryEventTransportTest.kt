package io.qpointz.mill.events.transport

import io.qpointz.mill.events.api.EventHandler
import io.qpointz.mill.events.api.EventSubscription
import io.qpointz.mill.events.dispatch.DirectEventDispatcher
import io.qpointz.mill.events.dispatch.ExecutorEventDispatcher
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.ProcessingMode
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.model.PublishOptions
import io.qpointz.mill.events.router.EventRouter
import io.qpointz.mill.events.testkit.TEST_EVENT_TYPE
import io.qpointz.mill.events.testkit.testEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class InMemoryEventTransportTest {

    @Test
    fun shouldDeliverEventToSubscribedHandler() {
        val received = CopyOnWriteArrayList<Event>()
        val router = EventRouter(
            listOf(EventSubscription(TEST_EVENT_TYPE, EventHandler { received.add(it) }, ProcessingMode.SYNC))
        )
        val transport = InMemoryEventTransport(DirectEventDispatcher(), router)

        val event = testEvent()
        transport.publish(event, PublishOptions(PublishMode.SYNC))

        assertThat(received).hasSize(1)
        assertThat(received[0].eventId).isEqualTo(event.eventId)
    }

    @Test
    fun shouldReturnBeforeHandlerCompletesOnAsyncPublish() {
        val latch = CountDownLatch(1)
        val executor = Executors.newSingleThreadExecutor()
        val router = EventRouter(
            listOf(EventSubscription(TEST_EVENT_TYPE, EventHandler {
                Thread.sleep(200)
                latch.countDown()
            }, ProcessingMode.SYNC))
        )
        val transport = InMemoryEventTransport(ExecutorEventDispatcher(executor), router)

        val start = System.currentTimeMillis()
        transport.publish(testEvent(), PublishOptions(PublishMode.ASYNC))
        val elapsed = System.currentTimeMillis() - start

        assertThat(elapsed).isLessThan(100)
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue()
        executor.shutdown()
    }

    @Test
    fun shouldBlockOnSyncPublishUntilHandlerCompletes() {
        val completed = CopyOnWriteArrayList<Boolean>()
        val router = EventRouter(
            listOf(EventSubscription(TEST_EVENT_TYPE, EventHandler {
                Thread.sleep(50)
                completed.add(true)
            }, ProcessingMode.SYNC))
        )
        val transport = InMemoryEventTransport(DirectEventDispatcher(), router)

        transport.publish(testEvent(), PublishOptions(PublishMode.SYNC))

        assertThat(completed).hasSize(1)
    }
}
