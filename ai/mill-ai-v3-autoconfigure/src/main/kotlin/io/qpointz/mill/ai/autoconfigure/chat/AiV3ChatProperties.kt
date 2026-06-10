package io.qpointz.mill.ai.autoconfigure.chat

import io.qpointz.mill.ai.chat.AiChatSettings
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Chat service configuration under `mill.ai.chat`.
 *
 * Example YAML:
 * ```yaml
 * mill:
 *   ai:
 *     chat:
 *       model: default
 *       default-profile: hello-world
 *       default-user-id: default
 *       max-title-length: 30
 *       value-mapping:
 *         embedding: default
 * ```
 */
@ConfigurationProperties("mill.ai.chat")
data class AiV3ChatProperties(
    /**
     * Key into `mill.ai.models.chat.*` for the streaming chat model.
     */
    val model: String = "default",
    /**
     * Profile id assigned to new general chats when no profile is specified in the
     * create request. Must match a profile registered in [io.qpointz.mill.ai.profile.ProfileRegistry].
     */
    val defaultProfile: String = "hello-world",
    /**
     * Static user id returned by [PropertiesUserIdResolver].
     * Override the [UserIdResolver] bean for multi-user / authenticated deployments.
     */
    val defaultUserId: String = "default",
    /**
     * Maximum number of characters kept when auto-deriving a chat title from the
     * first user message. A trailing `…` is appended when the message is truncated.
     */
    val maxTitleLength: Int = 30,
    val valueMapping: ChatValueMappingCapabilities = ChatValueMappingCapabilities(),
    val schemaSearch: ChatSchemaSearchCapabilities = ChatSchemaSearchCapabilities(),
) {
    /**
     * @return framework-neutral settings for the unified chat service (`mill-ai-v3-service`)
     */
    fun toSettings(): AiChatSettings = AiChatSettings(
        defaultProfile = defaultProfile,
        defaultUserId = defaultUserId,
        maxTitleLength = maxTitleLength,
    )
}

/**
 * Capability hook: value-mapping embedding pipeline (`mill.ai.data.embedding.<profile>`).
 */
data class ChatValueMappingCapabilities(
    val embedding: String = "default",
)

/**
 * Reserved capability hook for schema-search embedding pipeline.
 */
data class ChatSchemaSearchCapabilities(
    val embedding: String? = null,
)
