package io.qpointz.mill.data.schema

import io.qpointz.mill.metadata.domain.MetadataEntity

/**
 * Logical model root merged with optional persisted metadata and resolved facets (SPEC §3f).
 *
 * @property metadataEntityId stable canonical URN ([SchemaModelRoot.ENTITY_ID]); unchanged when no row exists
 * @property metadata persisted entity row when present
 * @property facets effective facets for [metadata]; empty when [metadata] is null or has no facets
 */
data class ModelRootWithFacets(
    val metadataEntityId: String,
    override val metadata: MetadataEntity?,
    override val facets: SchemaFacets
) : WithFacets
