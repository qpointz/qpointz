package io.qpointz.mill.ai.core.capability

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
import org.junit.jupiter.api.Test

/** Basic smoke test for the minimal descriptor model. */
class CapabilityDescriptorTest {
    @Test
    fun `should keep capability metadata`() {
        val descriptor = CapabilityDescriptor(
            id = "conversation",
            name = "Conversation",
            description = "Core conversation capability",
            supportedContexts = setOf("general"),
            tags = setOf("core"),
        )

        assertEquals("conversation", descriptor.id)
        assertEquals(setOf("general"), descriptor.supportedContexts)
    }
}





