package io.qpointz.mill.ai.profile

import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.runtime.AgentContext

/**
 * The inputs required to resume a persisted chat at the runtime layer.
 *
 * Produced by [ProfileRegistry.rehydrate] from a [ChatMetadata] record.
 * Callers (e.g. ChatService) pass [profile] and [agentContext] directly into the
 * agent without re-inferring them from transcript or routing rules.
 */
data class ChatRehydrationContext(
    val metadata: ChatMetadata,
    val profile: AgentProfile,
    val agentContext: AgentContext,
)

/**
 * Reconstructs a [ChatRehydrationContext] from persisted [ChatMetadata].
 *
 * Returns null if the persisted [ChatMetadata.profileId] cannot be resolved,
 * which means the profile was removed or renamed after the chat was created.
 * The caller decides whether to fall back to a default or surface an error.
 *
 * Field mapping:
 * - [ChatMetadata.profileId]        → resolved [AgentProfile]
 * - [ChatMetadata.contextType]      → [AgentContext.contextType]  (defaults to "general")
 * - [ChatMetadata.contextEntityType]→ [AgentContext.focusEntityType]
 * - [ChatMetadata.contextId]        → [AgentContext.focusEntityId]
 */
fun ProfileRegistry.rehydrate(metadata: ChatMetadata): ChatRehydrationContext? {
    val profile = resolve(metadata.profileId) ?: return null
    val agentContext = AgentContext(
        contextType = metadata.contextType ?: "general",
        focusEntityType = metadata.contextEntityType,
        focusEntityId = metadata.contextId,
    )
    return ChatRehydrationContext(
        metadata = metadata,
        profile = profile,
        agentContext = agentContext,
    )
}
