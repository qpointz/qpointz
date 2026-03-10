package io.qpointz.mill.ai

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
