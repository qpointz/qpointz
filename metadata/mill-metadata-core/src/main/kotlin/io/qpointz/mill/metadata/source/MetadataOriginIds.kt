package io.qpointz.mill.metadata.source

/**
 * Well-known [MetadataSource.originId] values.
 */
object MetadataOriginIds {
    /** Persisted facet rows loaded via [io.qpointz.mill.metadata.repository.FacetReadSide] / [RepositoryMetadataSource]. */
    const val REPOSITORY_LOCAL: String = "repository-local"

    /** Inferred layout from [io.qpointz.mill.data.backend.SchemaProvider] (SPEC §3g). */
    const val LOGICAL_LAYOUT: String = "logical-layout"
}
