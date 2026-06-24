package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.io.MetadataYamlSerializer
import io.qpointz.mill.utils.JsonUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Validates [platform-dq-l1-facet-types.yaml] parses and each definition passes
 * [FacetTypeManifestNormalizer] strict checks (WI-342 / platform DQ L1 seeds).
 */
class PlatformDqL1FacetTypesSeedTest {

    private val mapper = JsonUtils.defaultJsonMapper()

    private val expectedSlugs = setOf(
        "dq-null-check",
        "dq-empty-value-check",
        "dq-unique-value-check",
        "dq-allowed-values-check",
        "dq-pattern-check",
        "dq-min-max-check",
        "dq-data-age-check",
        "dq-referential-integrity",
        "dq-referential-source",
        "dq-referential-target"
    )

    private val expectedApplicableTo = mapOf(
        "dq-null-check" to listOf("urn:mill/metadata/entity-type:attribute"),
        "dq-empty-value-check" to listOf("urn:mill/metadata/entity-type:attribute"),
        "dq-unique-value-check" to listOf("urn:mill/metadata/entity-type:attribute"),
        "dq-allowed-values-check" to listOf("urn:mill/metadata/entity-type:attribute"),
        "dq-pattern-check" to listOf("urn:mill/metadata/entity-type:attribute"),
        "dq-min-max-check" to listOf("urn:mill/metadata/entity-type:attribute"),
        "dq-data-age-check" to listOf("urn:mill/metadata/entity-type:table"),
        "dq-referential-integrity" to listOf("urn:mill/metadata/entity-type:schema"),
        "dq-referential-source" to listOf("urn:mill/metadata/entity-type:table"),
        "dq-referential-target" to listOf("urn:mill/metadata/entity-type:table")
    )

    @Test
    fun shouldLoadAndNormalizeAllDqL1FacetTypeDefinitions() {
        val yaml = readSeedResource()
        val doc = MetadataYamlSerializer.deserialize(yaml)
        val definitions = doc.definitions

        assertThat(definitions).hasSize(10)
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

            if (slug == "dq-pattern-check") {
                val dialectField = manifest.payload.fields.orEmpty().first { it.name == "patternDialect" }
                assertThat(dialectField.schema.default).isEqualTo("sql_like")
                val dialectValues = dialectField.schema.values.orEmpty().map { it.value }
                assertThat(dialectValues).containsExactly("sql_like", "sql_regex")
            }

            if (slug == "dq-data-age-check") {
                val names = manifest.payload.fields.orEmpty().map { it.name }
                assertThat(names).contains("expectedScheduleCron", "expectedScheduleTimezone")
                assertThat(manifest.payload.fields.orEmpty().first { it.name == "expectedScheduleCron" }.required).isFalse()
                assertThat(manifest.payload.fields.orEmpty().first { it.name == "expectedScheduleTimezone" }.required).isFalse()
            }

            val enabledField = manifest.payload.fields.orEmpty().first { it.name == "enabled" }
            assertThat(enabledField.schema.default).isEqualTo(true)
        }
    }

    @Test
    fun shouldDeclareOptionalJoinSqlOnAllReferentialTypes() {
        val yaml = readSeedResource()
        val doc = MetadataYamlSerializer.deserialize(yaml)
        val referential = doc.definitions.filter { slug(it.typeKey).startsWith("dq-referential") }

        assertThat(referential).hasSize(3)
        for (def in referential) {
            val manifest = definitionToManifest(def)
            val names = manifest.payload.fields.orEmpty().map { it.name }
            assertThat(names).contains("joinSql")
        }
    }

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
        val stream = javaClass.classLoader.getResourceAsStream("metadata/platform-dq-l1-facet-types.yaml")
            ?: error("Missing classpath resource metadata/platform-dq-l1-facet-types.yaml")
        return stream.bufferedReader().readText()
    }
}
