package io.qpointz.mill.ai.mcp.transport.http.support

import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityDescriptor
import io.qpointz.mill.ai.core.capability.CapabilityProvider
import io.qpointz.mill.ai.core.prompt.PromptAsset
import io.qpointz.mill.ai.core.protocol.ProtocolDefinition
import io.qpointz.mill.ai.core.tool.ToolBinding
import io.qpointz.mill.ai.runtime.AgentContext

/**
 * Test-only capability provider for MCP exposure opt-out integration coverage.
 */
class TestMcpDisabledCapabilityProvider : CapabilityProvider {
  override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
    id = "test-mcp-disabled",
    name = "Disabled",
    description = "Disabled MCP capability",
    supportedContexts = setOf("general"),
  )

  override fun create(
    context: AgentContext,
    dependencies: CapabilityDependencies,
  ): io.qpointz.mill.ai.core.capability.Capability =
    object : io.qpointz.mill.ai.core.capability.Capability {
      override val descriptor = this@TestMcpDisabledCapabilityProvider.descriptor()
      override val tools = emptyList<ToolBinding>()
      override val prompts = emptyList<PromptAsset>()
      override val protocols = emptyList<ProtocolDefinition>()
    }
}
