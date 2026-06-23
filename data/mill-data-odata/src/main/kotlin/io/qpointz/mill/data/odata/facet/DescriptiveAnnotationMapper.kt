package io.qpointz.mill.data.odata.facet

import io.qpointz.mill.metadata.domain.core.DescriptiveFacet

/**
 * Extracts descriptive text from Mill facets for OData documentation.
 *
 * <p>RWS 2.16 programmatic EDM builders do not expose annotation attachment; descriptions are
 * surfaced in Mill metadata and may be added to CSDL in a follow-up when annotation factories are wired.
 */
object DescriptiveAnnotationMapper {

    /**
     * @param descriptive optional descriptive facet
     * @return description text when present
     */
    fun descriptionText(descriptive: DescriptiveFacet?): String? {
        if (descriptive == null) {
            return null
        }
        return descriptive.description?.takeIf { it.isNotBlank() }
            ?: descriptive.displayName?.takeIf { it.isNotBlank() }
    }
}
