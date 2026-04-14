package io.qpointz.mill.ai.chat

/**
 * Framework-neutral chat service settings (title derivation, default profile, user id).
 *
 * Spring Boot hosts bind these from `mill.ai.chat` via `MillAiV3ChatProperties` in `mill-ai-v3-autoconfigure`.
 *
 * @property defaultProfile profile id for new general chats when none is specified
 * @property defaultUserId default subject when using [PropertiesUserIdResolver]
 * @property maxTitleLength max characters when auto-deriving a chat title from the first message
 */
data class MillAiChatSettings(
    val defaultProfile: String = "hello-world",
    val defaultUserId: String = "default",
    val maxTitleLength: Int = 30,
)
