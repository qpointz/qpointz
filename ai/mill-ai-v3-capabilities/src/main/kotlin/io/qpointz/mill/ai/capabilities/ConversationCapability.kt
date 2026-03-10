package io.qpointz.mill.ai.capabilities

import io.qpointz.mill.ai.AgentContext
import io.qpointz.mill.ai.Capability
import io.qpointz.mill.ai.CapabilityDescriptor
import io.qpointz.mill.ai.CapabilityProvider
import io.qpointz.mill.ai.PromptAsset
import io.qpointz.mill.ai.ProtocolDefinition
import io.qpointz.mill.ai.ToolDefinition

/**
 * Minimal conversation capability used by the hello-world profile.
 *
 * It contributes prompt/protocol assets only; tool execution is delegated to the demo
 * capability.
 */
class ConversationCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "conversation",
        name = "Conversation",
        description = "Minimal conversation capability skeleton for ai/v3.",
        supportedContexts = setOf("general"),
        tags = setOf("core", "conversation"),
    )

    override fun create(context: AgentContext): Capability = SimpleCapability(descriptor())
}

/** Simple immutable capability instance returned by the provider. */
private data class SimpleCapability(
    override val descriptor: CapabilityDescriptor,
) : Capability {
    override val prompts: List<PromptAsset> = listOf(
        PromptAsset(
            id = "conversation.system",
            description = "Minimal system guidance for user-facing conversation.",
            content = "Be concise, user-facing, and stream progress without exposing hidden reasoning.",
        ),
        PromptAsset(
            id = "conversation.progress",
            description = "Guidance for short progress narration during streaming runs.",
            content = "When streaming progress, prefer short factual updates such as 'thinking' or 'checking demo tool'.",
        ),
    )

    override val tools: List<ToolDefinition> = emptyList()

    override val protocols: List<ProtocolDefinition> = listOf(
        ProtocolDefinition(
            id = "conversation.stream",
            description = "Minimal text/progress streaming protocol for hello-world validation.",
            eventTypes = listOf(
                "run.started",
                "thinking.delta",
                "plan.created",
                "reasoning.delta",
                "message.delta",
                "observation.made",
                "answer.completed",
            ),
        )
    )
}
