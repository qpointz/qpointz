package io.qpointz.mill.data.odata.annotation

import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet

/**
 * Maps [DescriptiveFacet] fields to [Org.OData.Core.V1](https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Core.V1.md) terms.
 */
object DescriptiveFacetAnnotationContributor : EdmFacetAnnotationContributor {

    /**
     * @param target model element receiving annotations
     * @param facets resolved facets for that element
     * @return Core vocabulary annotations derived from the descriptive facet
     */
    override fun contribute(target: EdmAnnotationTarget, facets: SchemaFacets): List<EdmCsdlAnnotation> {
        val descriptive = facets.descriptive ?: return emptyList()
        val annotations = mutableListOf<EdmCsdlAnnotation>()
        primaryDescription(descriptive)?.let {
            annotations += EdmCsdlAnnotation(ODataVocabularyTerms.CORE_DESCRIPTION, it)
        }
        descriptive.businessMeaning?.takeIf { it.isNotBlank() }?.let {
            annotations += EdmCsdlAnnotation(ODataVocabularyTerms.CORE_LONG_DESCRIPTION, it)
        }
        return annotations
    }

    /**
     * @param descriptive descriptive facet payload
     * @return short description text for {@link ODataVocabularyTerms#CORE_DESCRIPTION}
     */
    fun primaryDescription(descriptive: DescriptiveFacet): String? =
        descriptive.description?.takeIf { it.isNotBlank() }
            ?: descriptive.displayName?.takeIf { it.isNotBlank() }
}
