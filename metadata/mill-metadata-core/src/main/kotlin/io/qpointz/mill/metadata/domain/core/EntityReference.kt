package io.qpointz.mill.metadata.domain.core

/** Lightweight pointer to schema/table/optional attribute. */
data class EntityReference(
    val schema: String? = null,
    val table: String? = null,
    val attribute: String? = null
) {
    /** Fully-qualified reference string. */
    val fqn: String
        get() = if (!attribute.isNullOrEmpty()) "$schema.$table.$attribute" else "$schema.$table"

    /** Matches this reference against provided location components. */
    fun matches(schema: String?, table: String?, attribute: String?): Boolean =
        this.schema == schema && this.table == table &&
                (attribute == null || this.attribute == null || this.attribute == attribute)
}
