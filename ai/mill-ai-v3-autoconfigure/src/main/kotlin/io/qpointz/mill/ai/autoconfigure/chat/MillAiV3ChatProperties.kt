package io.qpointz.mill.ai.autoconfigure.chat

import io.qpointz.mill.ai.chat.MillAiChatSettings
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Chat service configuration under `mill.ai.chat`.
 *
 * Example YAML:
 * ```yaml
 * mill:
 *   ai:
 *     chat:
 *       default-profile: hello-world
 *       default-user-id: default
 *       max-title-length: 30
 * ```
 */
@ConfigurationProperties("mill.ai.chat")
data class MillAiV3ChatProperties(
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
) {
    /**
     * @return framework-neutral settings for the unified chat service (`mill-ai-v3-service`)
     */
    fun toSettings(): MillAiChatSettings = MillAiChatSettings(
        defaultProfile = defaultProfile,
        defaultUserId = defaultUserId,
        maxTitleLength = maxTitleLength,
    )
}
