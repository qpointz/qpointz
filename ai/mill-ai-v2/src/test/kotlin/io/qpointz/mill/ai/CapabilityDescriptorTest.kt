package io.qpointz.mill.ai

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CapabilityDescriptorTest {

    @Test
    fun `read descriptor from resource`() {
        val descriptor = ContextDescriptor.fromResource("test-capability.yaml")
        assertNotNull(descriptor)
        assertEquals("Test Capability", descriptor.name)
        assertEquals("This is a test capability.", descriptor.description)
    }
}