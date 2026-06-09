package io.qpointz.mill.analysis.queries

import java.time.Instant

/**
 * Domain record for a persisted saved SQL query (Analysis catalog).
 *
 * @param id stable catalog identifier (URL segment)
 * @param name display title in the Analysis sidebar
 * @param description optional summary shown in the editor header
 * @param sql SQL text loaded into the editor
 * @param createdAt first persistence time (UTC)
 * @param updatedAt last metadata or SQL change time (UTC)
 * @param tags optional labels for search and grouping
 */
data class SavedQuery(
    val id: String,
    val name: String,
    val description: String?,
    val sql: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val tags: List<String> = emptyList(),
)
