package io.qpointz.mill.ai.capabilities

import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.CapabilityRegistry
import io.qpointz.mill.ai.HelloWorldAgentProfile
import io.qpointz.mill.ai.HelloWorldCapabilitySet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Locks the hello-world capability packaging/discovery shape before runtime work expands.
 */
class CapabilityRegistryTest {
    @Test
    fun `should discover hello world capabilities through service loader`() {
        val registry = CapabilityRegistry.load(javaClass.classLoader)

        val descriptors = registry.allDescriptors()

        assertTrue(descriptors.map { it.id }.toSet().containsAll(HelloWorldCapabilitySet.requiredCapabilityIds))
    }

    @Test
    fun `should expose minimal capability assets for hello world`() {
        val registry = CapabilityRegistry.load(javaClass.classLoader)
        val capabilities = registry.capabilitiesFor(
            HelloWorldAgentProfile.profile,
            AgentContext(contextType = "general"),
        )
            .associateBy { it.descriptor.id }

        val conversation = requireNotNull(capabilities["conversation"])
        val demo = requireNotNull(capabilities["demo"])

        assertTrue(conversation.prompts.isNotEmpty())
        assertTrue(conversation.protocols.isNotEmpty())
        assertTrue(demo.prompts.isNotEmpty())
        assertEquals(setOf("say_hello", "echo_text", "noop", "list_demo_capabilities"), demo.tools.map { it.name }.toSet())
    }
}
