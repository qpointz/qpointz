package io.qpointz.mill.ai.profile

import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.runtime.AgentContext
import io.qpointz.mill.ai.runtime.AgentContextScope
import io.qpointz.mill.metadata.domain.MetadataUrns

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
 * Default metadata scopes for HTTP chat: global read + chat read-write.
 *
 * @param chatId conversation GUID
 */
fun defaultChatScopes(chatId: String): List<AgentContextScope> =
  listOf(
    AgentContextScope(MetadataUrns.SCOPE_GLOBAL, "r", "Global"),
    AgentContextScope(MetadataUrns.scopeChat(chatId), "rw", "Chat"),
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
 * - [ChatMetadata.chatId]           → [AgentContext.chatId] and default chat scopes
 */
fun ProfileRegistry.rehydrate(metadata: ChatMetadata): ChatRehydrationContext? {
  val profile = resolve(metadata.profileId) ?: return null
  val agentContext = AgentContext(
    contextType = metadata.contextType ?: "general",
    focusEntityType = metadata.contextEntityType,
    focusEntityId = metadata.contextId,
    chatId = metadata.chatId,
    scopes = defaultChatScopes(metadata.chatId),
  )
  return ChatRehydrationContext(
    metadata = metadata,
    profile = profile,
    agentContext = agentContext,
  )
}
