package io.qpointz.mill.metadata.domain.core

/** Physical table category from the source backend. */
enum class TableType {
    TABLE, VIEW, MATERIALIZED_VIEW, SYSTEM_TABLE, TEMPORARY_TABLE
}
