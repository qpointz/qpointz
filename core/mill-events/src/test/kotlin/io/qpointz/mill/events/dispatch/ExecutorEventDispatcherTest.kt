package io.qpointz.mill.events.dispatch

import io.qpointz.mill.events.api.EventHandler
import io.qpointz.mill.events.api.EventSubscription
import io.qpointz.mill.events.model.Event
import io.qpointz.mill.events.model.ProcessingMode
import io.qpointz.mill.events.model.PublishMode
import io.qpointz.mill.events.router.EventRouter
import io.qpointz.mill.events.testkit.TEST_EVENT_TYPE
import io.qpointz.mill.events.testkit.testEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ExecutorEventDispatcherTest {

    private val executor = Executors.newSingleThreadExecutor()

    @AfterEach
    fun tearDown() {
        executor.shutdown()
    }

    @Test
    fun shouldDispatchAsyncOnExecutorThread() {
        val latch = CountDownLatch(1)
        val handlerThreads = CopyOnWriteArrayList<String>()
        val router = EventRouter(
            listOf(EventSubscription(TEST_EVENT_TYPE, EventHandler {
                handlerThreads.add(Thread.currentThread().name)
                latch.countDown()
            }, ProcessingMode.SYNC))
        )
        val dispatcher = ExecutorEventDispatcher(executor)

        dispatcher.dispatch(testEvent(), router, PublishMode.ASYNC)

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue()
        assertThat(handlerThreads[0]).isNotEqualTo(Thread.currentThread().name)
    }

    @Test
    fun shouldDispatchSyncOnCallerThread() {
        val handlerThreads = CopyOnWriteArrayList<String>()
        val router = EventRouter(
            listOf(EventSubscription(TEST_EVENT_TYPE, EventHandler {
                handlerThreads.add(Thread.currentThread().name)
            }, ProcessingMode.SYNC))
        )
        val dispatcher = ExecutorEventDispatcher(executor)

        dispatcher.dispatch(testEvent(), router, PublishMode.SYNC)

        assertThat(handlerThreads[0]).isEqualTo(Thread.currentThread().name)
    }

    @Test
    fun shouldReturnBeforeAsyncHandlerCompletes() {
        val latch = CountDownLatch(1)
        val router = EventRouter(
            listOf(EventSubscription(TEST_EVENT_TYPE, EventHandler {
                Thread.sleep(200)
                latch.countDown()
            }, ProcessingMode.SYNC))
        )
        val dispatcher = ExecutorEventDispatcher(executor)

        val start = System.currentTimeMillis()
        dispatcher.dispatch(testEvent(), router, PublishMode.ASYNC)
        val elapsed = System.currentTimeMillis() - start

        assertThat(elapsed).isLessThan(100)
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue()
    }
}
