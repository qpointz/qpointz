package io.qpointz.mill.ai

/**
 * Runtime context passed into capability/profile resolution.
 *
 * The initial hello-world flow uses only the coarse context type, but the same model leaves
 * room for future focused agents bound to a table, concept, or analysis object.
 */
data class AgentContext(
    val contextType: String,
    val focusEntityType: String? = null,
    val focusEntityId: String? = null,
)
