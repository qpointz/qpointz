package io.qpointz.mill.data.schema

/**
 * Physical catalog coordinates decoded from a **relational** metadata entity URN
 * (`urn:mill/metadata/entity:<schema>[.<table>[.<column>]]`).
 *
 * [schema] is always present for a valid relational path; [table] and [column] are null for
 * schema- or table-level entities. Non-relational locals (for example `concept:…`) decode to
 * all-null coordinates — callers must treat that as “no catalog binding”.
 *
 * @property schema physical schema name segment, if present
 * @property table physical table name segment, if present
 * @property column physical column name segment (may contain dots if encoded that way), if present
 */
data class CatalogPath(
    val schema: String?,
    val table: String?,
    val column: String?
)
