package io.qpointz.mill.ai.valuemap.refresh

/**
 * Lists attribute entity URNs that carry the primary `ai-column-value-mapping` facet (WI-182 discovery).
 */
fun interface ValueMappingIndexedAttributeDiscovery {

    /**
     * @return distinct `metadata_entity.entity_res` values in stable sort order
     */
    fun listAttributeUrns(): List<String>
}
