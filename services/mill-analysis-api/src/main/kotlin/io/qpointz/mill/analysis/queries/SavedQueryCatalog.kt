package io.qpointz.mill.analysis.queries

/**
 * Persistence port for the saved-query catalog backing {@code /api/v1/analysis/queries}.
 */
interface SavedQueryCatalog {

    /**
     * @return all saved queries ordered by most recently updated first
     */
    fun findAll(): List<SavedQuery>

    /**
     * @param id catalog identifier
     * @return the query or {@code null} when not found
     */
    fun findById(id: String): SavedQuery?

    /**
     * Creates or updates a saved query keyed by {@link SavedQuery#id}.
     *
     * @param query domain record to persist
     * @return stored query (including timestamps from persistence)
     */
    fun save(query: SavedQuery): SavedQuery

    /**
     * @param id catalog identifier
     * @return {@code true} when a row was removed
     */
    fun deleteById(id: String): Boolean
}
