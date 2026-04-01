package io.qpointz.mill.metadata.io

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.facet.FacetAssignment

/**
 * Aggregated domain objects produced by [MetadataYamlSerializer.deserialize].
 *
 * @property scopes facet scope rows declared explicitly in YAML
 * @property definitions custom facet type definitions from `FacetTypeDefinition` documents
 * @property entities metadata entities (identity only; timestamps may be placeholders before import)
 * @property facetsByEntity facet assignments keyed by canonical entity URN (`entity_res`)
 */
data class MetadataYamlDocument(
    val scopes: List<MetadataScope>,
    val definitions: List<FacetTypeDefinition>,
    val entities: List<MetadataEntity>,
    val facetsByEntity: Map<String, List<FacetAssignment>>
)
