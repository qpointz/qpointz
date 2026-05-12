package io.qpointz.mill.data.query.engine

/**
 * Caller identity for session ownership checks.
 *
 * @property tenant Stable per-user tenant key (for example principal name or `sub`).
 */
data class CallerContext(
    val tenant: String,
)
