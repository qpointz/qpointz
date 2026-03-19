package io.qpointz.mill.ai.runtime.events.routing

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EventRoutingPolicyTest {

    private val rule = EventRoutingRule(
        eventType = "answer.completed",
        kind = "answer.completed",
        category = RoutedEventCategory.CHAT_TRANSCRIPT,
        destinations = setOf(
            RoutedEventDestination.CHAT_STREAM,
            RoutedEventDestination.CHAT_TRANSCRIPT,
            RoutedEventDestination.MODEL_MEMORY,
        ),
        persistAsTranscript = true,
    )

    private val policy = EventRoutingPolicy(rules = listOf(rule))

    @Test
    fun shouldReturnRule_whenEventTypeMatches() {
        val found = policy.ruleFor("answer.completed")
        assertEquals(rule, found)
    }

    @Test
    fun shouldReturnNull_whenEventTypeUnknown() {
        assertNull(policy.ruleFor("unknown.event"))
    }

    @Test
    fun shouldReturnCorrectDestinations() {
        val found = policy.ruleFor("answer.completed")!!
        assertTrue(found.destinations.contains(RoutedEventDestination.CHAT_TRANSCRIPT))
        assertTrue(found.destinations.contains(RoutedEventDestination.MODEL_MEMORY))
    }

    @Test
    fun shouldReturnCorrectPersistFlags() {
        val found = policy.ruleFor("answer.completed")!!
        assertTrue(found.persistAsTranscript, "expected persistAsTranscript")
        assertTrue(!found.persistAsArtifact, "expected no persistAsArtifact")
    }
}





