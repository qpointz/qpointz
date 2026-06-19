package io.qpointz.mill.events.router

import io.qpointz.mill.events.api.EventHandler
import io.qpointz.mill.events.api.EventSubscription
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.EventType
import io.qpointz.mill.events.model.ProcessingMode
import io.qpointz.mill.events.testkit.TEST_EVENT_TYPE
import io.qpointz.mill.events.testkit.testEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class EventRouterTest {

    @Test
    fun shouldDispatchToMatchingHandlers() {
        val received = CopyOnWriteArrayList<Event>()
        val router = EventRouter(
            listOf(
                EventSubscription(TEST_EVENT_TYPE, EventHandler { received.add(it) }, ProcessingMode.SYNC),
            )
        )

        val event = testEvent()
        router.dispatch(event)

        assertThat(received).hasSize(1)
        assertThat(received[0].eventId).isEqualTo(event.eventId)
    }

    @Test
    fun shouldMulticastToAllHandlersOnSameType() {
        val receivedA = CopyOnWriteArrayList<Event>()
        val receivedB = CopyOnWriteArrayList<Event>()
        val router = EventRouter(
            listOf(
                EventSubscription(TEST_EVENT_TYPE, EventHandler { receivedA.add(it) }, ProcessingMode.SYNC),
                EventSubscription(TEST_EVENT_TYPE, EventHandler { receivedB.add(it) }, ProcessingMode.SYNC),
            )
        )

        val event = testEvent()
        router.dispatch(event)

        assertThat(receivedA).hasSize(1)
        assertThat(receivedB).hasSize(1)
    }

    @Test
    fun shouldNotDispatchToUnmatchedType() {
        val received = CopyOnWriteArrayList<Event>()
        val otherType = EventType("other.type")
        val router = EventRouter(
            listOf(
                EventSubscription(otherType, EventHandler { received.add(it) }, ProcessingMode.SYNC),
            )
        )

        router.dispatch(testEvent())

        assertThat(received).isEmpty()
    }

    @Test
    fun shouldIsolateFailuresAcrossHandlers() {
        val receivedB = CopyOnWriteArrayList<Event>()
        val router = EventRouter(
            listOf(
                EventSubscription(TEST_EVENT_TYPE, EventHandler { throw RuntimeException("boom") }, ProcessingMode.SYNC),
                EventSubscription(TEST_EVENT_TYPE, EventHandler { receivedB.add(it) }, ProcessingMode.SYNC),
            )
        )

        router.dispatch(testEvent())

        assertThat(receivedB).hasSize(1)
    }

    @Test
    fun shouldRunAsyncHandlersOnExecutor() {
        val executor = Executors.newSingleThreadExecutor()
        val latch = CountDownLatch(1)
        val handlerThread = CopyOnWriteArrayList<String>()

        val router = EventRouter(
            listOf(
                EventSubscription(TEST_EVENT_TYPE, EventHandler {
                    handlerThread.add(Thread.currentThread().name)
                    latch.countDown()
                }, ProcessingMode.ASYNC),
            ),
            asyncExecutor = executor,
        )

        router.dispatch(testEvent())
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue()
        assertThat(handlerThread[0]).isNotEqualTo(Thread.currentThread().name)

        executor.shutdown()
    }

    @Test
    fun shouldRunSyncHandlersOnCallingThread() {
        val handlerThread = CopyOnWriteArrayList<String>()
        val router = EventRouter(
            listOf(
                EventSubscription(TEST_EVENT_TYPE, EventHandler {
                    handlerThread.add(Thread.currentThread().name)
                }, ProcessingMode.SYNC),
            )
        )

        router.dispatch(testEvent())

        assertThat(handlerThread[0]).isEqualTo(Thread.currentThread().name)
    }

    @Test
    fun shouldTreatAfterCommitAsSyncInMemory() {
        val handlerThread = CopyOnWriteArrayList<String>()
        val router = EventRouter(
            listOf(
                EventSubscription(TEST_EVENT_TYPE, EventHandler {
                    handlerThread.add(Thread.currentThread().name)
                }, ProcessingMode.AFTER_COMMIT),
            )
        )

        router.dispatch(testEvent())

        assertThat(handlerThread[0]).isEqualTo(Thread.currentThread().name)
    }
}
