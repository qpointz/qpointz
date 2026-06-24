package io.qpointz.mill.data.odata.annotation

import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DescriptiveFacetAnnotationContributorTest {

    @Test
    fun shouldMapDescriptionToCoreDescriptionTerm() {
        val facets = SchemaFacets(
            setOf(
                DescriptiveFacet(
                    description = "Airport cities served by the airline.",
                ),
            ),
        )

        val annotations = DescriptiveFacetAnnotationContributor.contribute(
            EdmAnnotationTarget.EntityType("skymill", "cities"),
            facets,
        )

        assertThat(annotations).containsExactly(
            EdmCsdlAnnotation(ODataVocabularyTerms.CORE_DESCRIPTION, "Airport cities served by the airline."),
        )
    }

    @Test
    fun shouldMapBusinessMeaningToCoreLongDescriptionTerm() {
        val facets = SchemaFacets(
            setOf(
                DescriptiveFacet(
                    description = "City name.",
                    businessMeaning = "Human-readable city label for reporting.",
                ),
            ),
        )

        val annotations = DescriptiveFacetAnnotationContributor.contribute(
            EdmAnnotationTarget.StructuralProperty("skymill", "cities", "city"),
            facets,
        )

        assertThat(annotations).containsExactly(
            EdmCsdlAnnotation(ODataVocabularyTerms.CORE_DESCRIPTION, "City name."),
            EdmCsdlAnnotation(
                ODataVocabularyTerms.CORE_LONG_DESCRIPTION,
                "Human-readable city label for reporting.",
            ),
        )
    }

    @Test
    fun shouldFallBackToDisplayNameForDescription() {
        val facets = SchemaFacets(setOf(DescriptiveFacet(displayName = "Cities")))

        val annotations = DescriptiveFacetAnnotationContributor.contribute(
            EdmAnnotationTarget.EntityType("skymill", "cities"),
            facets,
        )

        assertThat(annotations).containsExactly(
            EdmCsdlAnnotation(ODataVocabularyTerms.CORE_DESCRIPTION, "Cities"),
        )
    }
}
