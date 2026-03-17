package io.qpointz.mill.ai.capabilities

import io.qpointz.mill.ai.*

/**
 * Demo capability used to exercise tool discovery, invocation, and mixed direct/tool flows.
 */
class DemoCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "demo",
        name = "Demo",
        description = "Minimal demo capability skeleton for ai/v3.",
        supportedContexts = setOf("general"),
        tags = setOf("demo"),
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability = DemoCapability(descriptor())
}

/** Simple immutable capability instance bundling trivial validation tools. */
private data class DemoCapability(
    override val descriptor: CapabilityDescriptor,
) : Capability {

    private val manifest = CapabilityManifest.load("capabilities/demo.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("say_hello") { request ->
            val name = request.arguments["name"]?.toString()?.ifBlank { "world" } ?: "world"
            ToolResult(mapOf("greeting" to "Hello, $name!"))
        },
        manifest.tool("echo_text") { request ->
            ToolResult(mapOf("echo" to (request.arguments["text"] ?: "")))
        },
        manifest.tool("noop") {
            ToolResult(mapOf("status" to "noop-complete"))
        },
        manifest.tool("list_demo_capabilities") {
            ToolResult(mapOf("capabilities" to listOf("conversation", "demo")))
        },
    )

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols
}
