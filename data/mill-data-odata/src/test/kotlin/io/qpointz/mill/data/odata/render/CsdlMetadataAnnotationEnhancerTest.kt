package io.qpointz.mill.data.odata.render

import io.qpointz.mill.data.odata.annotation.EdmAnnotationModel
import io.qpointz.mill.data.odata.annotation.EdmAnnotationTarget
import io.qpointz.mill.data.odata.annotation.EdmCsdlAnnotation
import io.qpointz.mill.data.odata.annotation.EdmAnnotationMapper
import io.qpointz.mill.data.odata.annotation.ODataVocabularyTerms
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CsdlMetadataAnnotationEnhancerTest {

    @Test
    fun shouldInjectCoreVocabularyReferenceAndEntityAnnotations() {
        val baseXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <edmx:Edmx Version="4.0" xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx">
              <edmx:DataServices>
                <Schema Namespace="Mill.skymill" xmlns="http://docs.oasis-open.org/odata/ns/edm">
                  <EntityType Name="cities">
                    <Key>
                      <PropertyRef Name="id"/>
                    </Key>
                    <Property Name="city" Type="Edm.String" Nullable="true"/>
                  </EntityType>
                </Schema>
              </edmx:DataServices>
            </edmx:Edmx>
        """.trimIndent()

        val builder = EdmAnnotationModel.Builder()
        builder.addFromFacets(
            EdmAnnotationTarget.EntityType("skymill", "cities"),
            io.qpointz.mill.data.schema.SchemaFacets(
                setOf(
                    io.qpointz.mill.metadata.domain.core.DescriptiveFacet(
                        description = "Airport cities served by the airline.",
                    ),
                ),
            ),
            EdmAnnotationMapper(),
        )
        val annotations = builder.build()

        val enhanced = CsdlMetadataAnnotationEnhancer.enhance(baseXml, annotations)

        assertThat(enhanced).contains(ODataVocabularyTerms.CORE_VOCABULARY_REFERENCE_URI)
        assertThat(enhanced).contains("""Term="${ODataVocabularyTerms.CORE_DESCRIPTION}"""")
        assertThat(enhanced).contains("""String="Airport cities served by the airline."""")
    }

    @Test
    fun shouldInjectPropertyAnnotationsBeforeClosingTag() {
        val baseXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <edmx:Edmx Version="4.0" xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx">
              <edmx:DataServices>
                <Schema Namespace="Mill.skymill" xmlns="http://docs.oasis-open.org/odata/ns/edm">
                  <EntityType Name="cities">
                    <Key>
                      <PropertyRef Name="id"/>
                    </Key>
                    <Property Name="city" Type="Edm.String" Nullable="true"/>
                  </EntityType>
                </Schema>
              </edmx:DataServices>
            </edmx:Edmx>
        """.trimIndent()

        val builder = EdmAnnotationModel.Builder()
        builder.addFromFacets(
            EdmAnnotationTarget.StructuralProperty("skymill", "cities", "city"),
            io.qpointz.mill.data.schema.SchemaFacets(
                setOf(io.qpointz.mill.metadata.domain.core.DescriptiveFacet(description = "City name.")),
            ),
            EdmAnnotationMapper(),
        )
        val enhanced = CsdlMetadataAnnotationEnhancer.enhance(baseXml, builder.build())

        assertThat(enhanced).contains(ODataVocabularyTerms.CORE_VOCABULARY_REFERENCE_URI)
        assertThat(enhanced).contains("""Term="${ODataVocabularyTerms.CORE_DESCRIPTION}"""")
        assertThat(enhanced).contains("""String="City name."""")
        assertThat(enhanced).contains("""Name="city"""")
    }
}
