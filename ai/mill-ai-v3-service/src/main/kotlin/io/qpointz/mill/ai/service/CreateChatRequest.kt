package io.qpointz.mill.ai.service

/**
 * Input for [UnifiedChatService.createChat].
 *
 * Pass `null` to create a general (non-contextual) chat.
 * Populate the context fields to create or reuse a context-bound chat.
 */
data class CreateChatRequest(
    /** Profile id override. When null the service uses [io.qpointz.mill.ai.autoconfigure.chat.MillAiV3ChatProperties.defaultProfile]. */
    val profileId: String? = null,
    /**
     * Domain context type (e.g. `"model"`, `"dashboard"`).
     * Must be non-null when [contextId] is set.
     */
    val contextType: String? = null,
    /** Domain context entity id (e.g. a model qualified name). */
    val contextId: String? = null,
    /** Human-readable label for the context entity shown in the UI. */
    val contextLabel: String? = null,
    /** Optional entity type within the context (e.g. `"table"`, `"column"`). */
    val contextEntityType: String? = null,
)
