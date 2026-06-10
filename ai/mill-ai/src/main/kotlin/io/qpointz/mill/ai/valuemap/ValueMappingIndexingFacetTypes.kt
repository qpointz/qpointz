package io.qpointz.mill.ai.valuemap

import io.qpointz.mill.metadata.domain.MetadataUrns

/**
 * Canonical facet type URNs for value-mapping indexing (WI-181 / WI-182).
 *
 * Delegates to [MetadataUrns] so AI modules do not duplicate URN literals.
 */
object ValueMappingIndexingFacetTypes {
    const val AI_COLUMN_VALUE_MAPPING: String = MetadataUrns.FACET_TYPE_AI_COLUMN_VALUE_MAPPING
    const val AI_COLUMN_VALUE_MAPPING_VALUES: String = MetadataUrns.FACET_TYPE_AI_COLUMN_VALUE_MAPPING_VALUES
}
