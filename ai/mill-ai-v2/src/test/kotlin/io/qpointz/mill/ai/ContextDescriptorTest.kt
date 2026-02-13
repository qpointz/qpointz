package io.qpointz.mill.ai

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ContextDescriptorTest {

    @Test
    fun `read descriptor from resource`() {
        val descriptor = ContextDescriptor.fromResource("capabilities/testcapability/trivia.yml")
        assertNotNull(descriptor)
        assertEquals("test-trivia", descriptor.name)
        assertEquals("test-description", descriptor.description)
        assertEquals("test-system", descriptor.system)
    }

}