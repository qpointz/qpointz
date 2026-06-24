package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.io.MetadataYamlSerializer
import io.qpointz.mill.utils.JsonUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Validates [platform-dq-l2-facet-types.yaml] parses and each definition passes
 * [FacetTypeManifestNormalizer] strict checks (WI-344 / platform DQ L2 seeds).
 */
class PlatformDqL2FacetTypesSeedTest {

    private val mapper = JsonUtils.defaultJsonMapper()

    private val expectedSlugs = setOf(
        "dq-predicate",
        "dq-composite-uniqueness",
        "dq-parent-child-reconciliation",
        "dq-cross-table-reconciliation",
        "dq-sla-compliance-check"
    )

    private val expectedApplicableTo = mapOf(
        "dq-predicate" to listOf("urn:mill/metadata/entity-type:table"),
        "dq-composite-uniqueness" to listOf("urn:mill/metadata/entity-type:table"),
        "dq-parent-child-reconciliation" to listOf("urn:mill/metadata/entity-type:table"),
        "dq-cross-table-reconciliation" to listOf("urn:mill/metadata/entity-type:table"),
        "dq-sla-compliance-check" to listOf("urn:mill/metadata/entity-type:table")
    )

    @Test
    fun shouldLoadAndNormalizeAllDqL2FacetTypeDefinitions() {
        val yaml = readSeedResource()
        val doc = MetadataYamlSerializer.deserialize(yaml)
        val definitions = doc.definitions

        assertThat(definitions).hasSize(5)
        assertThat(definitions.map { slug(it.typeKey) }).containsExactlyInAnyOrderElementsOf(expectedSlugs)

        for (def in definitions) {
            val slug = slug(def.typeKey)
            assertThat(def.category).isEqualTo("data-quality")
            assertThat(def.mandatory).isFalse()
            assertThat(def.enabled).isTrue()
            assertThat(def.targetCardinality).isEqualTo(FacetTargetCardinality.MULTIPLE)
            assertThat(def.applicableTo).isEqualTo(expectedApplicableTo[slug])
            assertThat(def.displayName).isNotBlank
            assertThat(def.description).isNotBlank
            assertThat(def.contentSchema).isNotNull

            val manifest = definitionToManifest(def)
            assertDoesNotThrow { FacetTypeManifestNormalizer.normalizeStrict(manifest) }

            val fieldNames = manifest.payload.fields.orEmpty().map { it.name }.toSet()
            assertThat(fieldNames)
                .withFailMessage("common DQ envelope missing on $slug")
                .contains("name", "description", "severity", "profile", "enabled", "tags")

            val tagsField = manifest.payload.fields.orEmpty().first { it.name == "tags" }
            assertThat(tagsField.stereotype).containsExactly("tags")

            val enabledField = manifest.payload.fields.orEmpty().first { it.name == "enabled" }
            assertThat(enabledField.schema.default).isEqualTo(true)
        }
    }

    @Test
    fun shouldDeclareRequiredRuleFieldsPerL2Type() {
        val yaml = readSeedResource()
        val doc = MetadataYamlSerializer.deserialize(yaml)
        val bySlug = doc.definitions.associateBy { slug(it.typeKey) }

        assertThat(bySlug["dq-predicate"]!!.let { requiredFields(it) }).contains("predicate")
        assertThat(bySlug["dq-composite-uniqueness"]!!.let { requiredFields(it) }).contains("columns")
        assertThat(bySlug["dq-parent-child-reconciliation"]!!.let { requiredFields(it) })
            .contains("child", "parentKey", "parentMeasure")
        val childSchema = definitionToManifest(bySlug["dq-parent-child-reconciliation"]!!)
            .payload.fields.orEmpty()
            .first { it.name == "child" }
            .schema
        val childRequired = childSchema.fields.orEmpty()
            .filter { it.required == true }
            .map { it.name }
            .toSet()
        assertThat(childRequired).containsExactlyInAnyOrder("schema", "table", "foreignKey", "aggregate")
        assertThat(bySlug["dq-cross-table-reconciliation"]!!.let { requiredFields(it) })
            .contains("other", "aggregate")
        val otherSchema = definitionToManifest(bySlug["dq-cross-table-reconciliation"]!!)
            .payload.fields.orEmpty()
            .first { it.name == "other" }
            .schema
        val otherRequired = otherSchema.fields.orEmpty()
            .filter { it.required == true }
            .map { it.name }
            .toSet()
        assertThat(otherRequired).containsExactlyInAnyOrder("schema", "table", "aggregate")
        assertThat(bySlug["dq-sla-compliance-check"]!!.let { requiredFields(it) })
            .contains("timestampColumn", "deadlineLocalTime")
    }

    private fun requiredFields(def: FacetTypeDefinition): Set<String> =
        definitionToManifest(def).payload.fields.orEmpty()
            .filter { it.required == true }
            .map { it.name }
            .toSet()

    private fun definitionToManifest(def: FacetTypeDefinition): FacetTypeManifest {
        val payload = mapper.convertValue(def.contentSchema, FacetPayloadSchema::class.java)
        return FacetTypeManifest(
            typeKey = def.typeKey,
            title = def.displayName ?: def.typeKey,
            description = def.description ?: "",
            category = def.category,
            enabled = def.enabled,
            mandatory = def.mandatory,
            targetCardinality = def.targetCardinality,
            applicableTo = def.applicableTo,
            schemaVersion = def.schemaVersion,
            payload = payload
        )
    }

    private fun slug(typeKey: String): String =
        typeKey.removePrefix("urn:mill/metadata/facet-type:")

    private fun readSeedResource(): String {
        val stream = javaClass.classLoader.getResourceAsStream("metadata/platform-dq-l2-facet-types.yaml")
            ?: error("Missing classpath resource metadata/platform-dq-l2-facet-types.yaml")
        return stream.bufferedReader().readText()
    }
}
