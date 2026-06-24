package io.qpointz.mill.data.odata.annotation

import io.qpointz.mill.data.schema.SchemaFacets

/**
 * Collects CSDL annotations from registered [EdmFacetAnnotationContributor] instances.
 *
 * @param contributors ordered facet mappers; defaults to descriptive facet support
 */
class EdmAnnotationMapper(
    private val contributors: List<EdmFacetAnnotationContributor> = DEFAULT_CONTRIBUTORS,
) {

    /**
     * @param target model element receiving annotations
     * @param facets resolved facets for that element
     * @return merged annotations from all contributors
     */
    fun map(target: EdmAnnotationTarget, facets: SchemaFacets): List<EdmCsdlAnnotation> =
        contributors.flatMap { it.contribute(target, facets) }

    companion object {
        private val DEFAULT_CONTRIBUTORS =
            listOf<EdmFacetAnnotationContributor>(
                DescriptiveFacetAnnotationContributor,
            )
    }
}
