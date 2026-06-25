package io.qpointz.mill.metadata.repository

import io.qpointz.mill.metadata.domain.MetadataContent

/**
 * Persistence for {@code metadata_content} authoring rows.
 */
interface MetadataContentRepository {

    /**
     * @param contentUrn canonical content URN
     * @return row or {@code null}
     */
    fun findByContentUrn(contentUrn: String): MetadataContent?

    /**
     * Lists rows for a target, optionally filtered by {@code contentKind}.
     *
     * @param targetUrn facet type or category URN
     * @param contentKind optional kind filter; {@code null} = all kinds
     */
    fun findByTarget(targetUrn: String, contentKind: String? = null): List<MetadataContent>

    /**
     * @return all rows (tests and export)
     */
    fun findAll(): List<MetadataContent>

    /**
     * @param content row to upsert by {@link MetadataContent#contentUrn}
     * @return persisted row
     */
    fun save(content: MetadataContent): MetadataContent
}
