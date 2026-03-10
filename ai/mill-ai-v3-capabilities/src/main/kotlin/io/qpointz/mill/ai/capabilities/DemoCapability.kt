package io.qpointz.mill.ai.capabilities

import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.CapabilityDescriptor
import io.qpointz.mill.ai.CapabilityProvider
import io.qpointz.mill.ai.PromptAsset
import io.qpointz.mill.ai.ProtocolDefinition
import io.qpointz.mill.ai.ToolDefinition
import io.qpointz.mill.ai.ToolField
import io.qpointz.mill.ai.ToolHandler
import io.qpointz.mill.ai.ToolResult

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

    override fun create(context: AgentContext): Capability = DemoCapability(descriptor())
}

/** Simple immutable capability instance bundling trivial validation tools. */
private data class DemoCapability(
    override val descriptor: CapabilityDescriptor,
) : Capability {
    override val prompts: List<PromptAsset> = listOf(
        PromptAsset(
            id = "demo.system",
            description = "Guidance for choosing between direct response and trivial demo tools.",
            content = "Use direct responses for simple acknowledgements, and use demo tools when the user asks for a demonstrable action or capability listing.",
        ),
    )

    override val tools: List<ToolDefinition> = listOf(
        // Greeting tool used by the integration test to prove tool-calling works end to end.
        ToolDefinition(
            name = "say_hello",
            description = "Return a friendly greeting for a provided name.",
            inputFields = listOf(
                ToolField("name", "Name to greet.")
            ),
            outputDescription = "Greeting payload containing the rendered greeting text.",
            handler = ToolHandler { request ->
                val name = request.arguments["name"]?.ifBlank { "world" } ?: "world"
                ToolResult(mapOf("greeting" to "Hello, $name!"))
            },
        ),
        // Echo tool gives the model a deterministic structured tool response.
        ToolDefinition(
            name = "echo_text",
            description = "Echo back user-provided text.",
            inputFields = listOf(
                ToolField("text", "Text to echo back.")
            ),
            outputDescription = "Echo payload containing the provided text.",
            handler = ToolHandler { request ->
                ToolResult(mapOf("echo" to (request.arguments["text"] ?: "")))
            },
        ),
        // No-op tool is useful when we want a real tool event without domain behavior.
        ToolDefinition(
            name = "noop",
            description = "Perform a no-op action that returns a deterministic marker.",
            outputDescription = "No-op payload indicating the action completed.",
            handler = ToolHandler {
                ToolResult(mapOf("status" to "noop-complete"))
            },
        ),
        // This makes the capability set self-describing through a trivial tool path.
        ToolDefinition(
            name = "list_demo_capabilities",
            description = "List the capabilities exposed by the demo hello-world setup.",
            outputDescription = "List of capability ids available in the hello-world setup.",
            handler = ToolHandler {
                ToolResult(mapOf("capabilities" to listOf("conversation", "demo")))
            },
        ),
    )

    override val protocols: List<ProtocolDefinition> = listOf(
        ProtocolDefinition(
            id = "demo.tool-events",
            description = "Minimal protocol for hello-world tool execution events.",
            eventTypes = listOf("tool.call", "tool.result"),
        )
    )
}
