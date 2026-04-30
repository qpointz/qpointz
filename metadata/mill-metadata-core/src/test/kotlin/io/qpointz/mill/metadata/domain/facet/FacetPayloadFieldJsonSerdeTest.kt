package io.qpointz.mill.metadata.domain.facet

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.kotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FacetPayloadFieldJsonSerdeTest {

    private val mapper = JsonMapper.builder()
        .addModule(
            kotlinModule {
                configure(KotlinFeature.NullIsSameAsDefault, true)
            }
        )
        .findAndAddModules()
        .build()

    private val sampleSchemaString = FacetPayloadSchema(
        type = FacetSchemaType.STRING,
        title = "S",
        description = "D"
    )

    private val sampleSchemaArray = FacetPayloadSchema(
        type = FacetSchemaType.ARRAY,
        title = "A",
        description = "AD",
        items = FacetPayloadSchema(
            type = FacetSchemaType.STRING,
            title = "I",
            description = "ID"
        )
    )

    @Test
    fun shouldDeserializeStereotypeCommaString() {
        val json =
            """{"name":"f","schema":{"type":"STRING","title":"S","description":"D"},"required":true,"stereotype":" a , b "}"""
        val f = mapper.readValue(json, FacetPayloadField::class.java)
        assertThat(f.stereotype).containsExactly("a", "b")
    }

    @Test
    fun shouldDeserializeStereotypeJsonArray() {
        val json =
            """{"name":"f","schema":{"type":"STRING","title":"S","description":"D"},"stereotype":["x","y"]}"""
        val f = mapper.readValue(json, FacetPayloadField::class.java)
        assertThat(f.stereotype).containsExactly("x", "y")
    }

    @Test
    fun shouldSerializeStereotypeAsCommaStringWhenSchemaNotArray() {
        val f = FacetPayloadField("f", sampleSchemaString, true, listOf("u", "v"))
        val tree = mapper.readTree(mapper.writeValueAsString(f))
        assertThat(tree.path("stereotype").asText()).isEqualTo("u,v")
    }

    @Test
    fun shouldSerializeStereotypeAsJsonArrayWhenSchemaIsArray() {
        val f = FacetPayloadField("f", sampleSchemaArray, true, listOf("p", "q"))
        val tree = mapper.readTree(mapper.writeValueAsString(f))
        val arr = tree.path("stereotype")
        assertThat(arr.isArray).isTrue()
        assertThat((arr as Iterable<JsonNode>).map { it.asText() }).containsExactly("p", "q")
    }
}
