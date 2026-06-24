package io.qpointz.mill.data.odata.annotation

import io.qpointz.mill.data.schema.SchemaFacets

/**
 * Maps one metadata facet type to zero or more CSDL annotations for a model element.
 *
 * <p>Register additional implementations on [EdmAnnotationMapper] to extend facet coverage
 * without changing EDM construction code.
 */
fun interface EdmFacetAnnotationContributor {

    /**
     * @param target model element receiving annotations
     * @param facets resolved facets for that element
     * @return CSDL annotations to attach to {@code target} in {@code $metadata}
     */
    fun contribute(target: EdmAnnotationTarget, facets: SchemaFacets): List<EdmCsdlAnnotation>
}
