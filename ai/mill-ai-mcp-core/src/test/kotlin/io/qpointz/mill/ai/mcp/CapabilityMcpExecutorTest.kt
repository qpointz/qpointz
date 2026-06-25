package io.qpointz.mill.ai.mcp

import io.qpointz.mill.ai.capabilities.DemoCapabilityProvider
import io.qpointz.mill.ai.core.capability.CapabilityDependencyContainer
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.profile.PlatformProfiles
import io.qpointz.mill.ai.runtime.AgentContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CapabilityMcpExecutorTest {

    private val registry = CapabilityRegistry.from(listOf(DemoCapabilityProvider()))
    private val context = AgentContext(
        contextType = "general",
        capabilityDependencies = CapabilityDependencyContainer.empty(),
    )

    @Test
    fun shouldCallDemoSayHello() {
        val catalog = CapabilityMcpCatalog(
            registry = registry,
            exposureConfig = McpExposureConfig(capabilities = listOf("demo")),
        )
        val executor = CapabilityMcpExecutor(registry, catalog, context)
        val result = executor.callTool("demo.say_hello", mapOf("name" to "Mill"))
        assertThat(result.content).isInstanceOf(Map::class.java)
        @Suppress("UNCHECKED_CAST")
        val map = result.content as Map<String, Any?>
        assertThat(map["greeting"]).isEqualTo("Hello, Mill!")
    }

    @Test
    fun shouldRejectUnknownTool() {
        val catalog = CapabilityMcpCatalog(registry = registry)
        val executor = CapabilityMcpExecutor(registry, catalog, context)
        assertThrows(McpToolInvocationException::class.java) {
            executor.callTool("demo.unknown")
        }
    }

    @Test
    fun shouldRejectToolOutsideProfileAtInvoke() {
        val catalog = CapabilityMcpCatalog(
            registry = CapabilityRegistry.load(),
            profile = PlatformProfiles.require("hello-world"),
        )
        val executor = CapabilityMcpExecutor(
            registry = CapabilityRegistry.load(),
            catalog = catalog,
            context = context,
        )
        assertThrows(McpToolInvocationException::class.java) {
            executor.callTool("schema.list_tables", mapOf("schemaName" to "main"))
        }
    }

    @Test
    fun shouldRejectToolOutsideAllowlistAtInvoke() {
        val catalog = CapabilityMcpCatalog(
            registry = registry,
            exposureConfig = McpExposureConfig(capabilities = listOf("conversation")),
        )
        val executor = CapabilityMcpExecutor(registry, catalog, context)
        assertThrows(McpToolInvocationException::class.java) {
            executor.callTool("demo.say_hello", mapOf("name" to "x"))
        }
    }

    @Test
    fun shouldRejectWhenAdmissionGateDenies() {
        val catalog = CapabilityMcpCatalog(
            registry = registry,
            exposureConfig = McpExposureConfig(capabilities = listOf("demo")),
        )
        val gate = CapabilityAdmissionGate { _, _ -> McpAdmissionDecision.DENY }
        val executor = CapabilityMcpExecutor(registry, catalog, context, gate)
        assertThrows(McpToolInvocationException::class.java) {
            executor.callTool("demo.say_hello", mapOf("name" to "x"))
        }
    }
}
