package io.qpointz.mill.data.schema.api.dto

/**
 * Active schema explorer context payload.
 *
 * @property selectedContext selected context slug used by UI for follow-up API calls
 * @property availableScopes list of currently available scope options
 */
data class SchemaContextDto(
    val selectedContext: String,
    val availableScopes: List<ScopeOptionDto>,
)

/**
 * Selectable scope option for schema explorer context.
 *
 * @property id stable identifier for UI option lookup
 * @property slug scope slug value used in query parameter
 * @property displayName human-readable label
 */
data class ScopeOptionDto(
    val id: String,
    val slug: String,
    val displayName: String,
)
