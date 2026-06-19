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
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList

class DirectEventDispatcherTest {

    @Test
    fun shouldDispatchSynchronouslyRegardlessOfPublishMode() {
        val received = CopyOnWriteArrayList<Event>()
        val router = EventRouter(
            listOf(EventSubscription(TEST_EVENT_TYPE, EventHandler { received.add(it) }, ProcessingMode.SYNC))
        )
        val dispatcher = DirectEventDispatcher()

        val event = testEvent()
        dispatcher.dispatch(event, router, PublishMode.ASYNC)

        assertThat(received).hasSize(1)
        assertThat(received[0].eventId).isEqualTo(event.eventId)
    }

    @Test
    fun shouldDispatchSyncModeOnCallerThread() {
        val received = CopyOnWriteArrayList<Event>()
        val router = EventRouter(
            listOf(EventSubscription(TEST_EVENT_TYPE, EventHandler { received.add(it) }, ProcessingMode.SYNC))
        )
        val dispatcher = DirectEventDispatcher()

        dispatcher.dispatch(testEvent(), router, PublishMode.SYNC)

        assertThat(received).hasSize(1)
    }
}
