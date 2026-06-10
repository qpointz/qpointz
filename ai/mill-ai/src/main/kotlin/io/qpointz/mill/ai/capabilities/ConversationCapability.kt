package io.qpointz.mill.ai.capabilities

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

/**
 * Minimal conversation capability used by the hello-world profile.
 *
 * It contributes prompt assets only; tool execution is delegated to the demo capability.
 */
class ConversationCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "conversation",
        name = "Conversation",
        description = "Minimal conversation capability skeleton for ai/v3.",
        supportedContexts = setOf("general"),
        tags = setOf("core", "conversation"),
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability = SimpleCapability(descriptor())
}

/** Simple immutable capability instance returned by the provider. */
private data class SimpleCapability(
    override val descriptor: CapabilityDescriptor,
) : Capability {

    private val manifest = CapabilityManifest.load("capabilities/conversation.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val tools: List<ToolBinding> = emptyList()

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols
}




