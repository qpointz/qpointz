package io.qpointz.mill.metadata.domain.core

/** Describes where a concept applies in the physical model. */
data class ConceptTarget(
    val schema: String? = null,
    val table: String? = null,
    val attributes: List<String> = emptyList()
) {
    /** Fully-qualified table name (`schema.table`). */
    val fqn: String get() = "$schema.$table"
}
