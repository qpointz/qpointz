package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.metadata.domain.FacetTypeDefinition
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.io.MetadataYamlSerializer
import io.qpointz.mill.utils.JsonUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.file.Path

/**
 * Validates [platform-bootstrap.yaml] and [platform-facet-types.json] declare
 * [MetadataUrns.FACET_TYPE_AI_ANNOTATION] with the WI-383 contract (WI-384).
 */
class PlatformAiAnnotationFacetTypeSeedTest {

    private val mapper = JsonUtils.defaultJsonMapper()

    private val expectedApplicableTo = listOf(
        "urn:mill/metadata/entity-type:schema",
        "urn:mill/metadata/entity-type:table",
        "urn:mill/metadata/entity-type:attribute",
    )

    @Test
    fun shouldLoadAndNormalizeAiAnnotationFromPlatformBootstrap() {
        val def = readBootstrapDefinition()
        assertThat(def.typeKey).isEqualTo(MetadataUrns.FACET_TYPE_AI_ANNOTATION)
        assertThat(def.category).isEqualTo("ai")
        assertThat(def.mandatory).isFalse()
        assertThat(def.enabled).isTrue()
        assertThat(def.targetCardinality).isEqualTo(FacetTargetCardinality.MULTIPLE)
        assertThat(def.applicableTo).isEqualTo(expectedApplicableTo)
        assertThat(def.displayName).isEqualTo("AI Annotation")
        assertThat(def.schemaVersion).isEqualTo("1.0")

        val manifest = definitionToManifest(def)
        assertDoesNotThrow { FacetTypeManifestNormalizer.normalizeStrict(manifest) }

        val fieldNames = manifest.payload.fields.orEmpty().map { it.name }.toSet()
        assertThat(fieldNames).containsExactlyInAnyOrder("title", "instruction", "kind", "tags", "enabled")

        val instructionField = manifest.payload.fields.orEmpty().first { it.name == "instruction" }
        assertThat(instructionField.required).isTrue()

        val kindField = manifest.payload.fields.orEmpty().first { it.name == "kind" }
        assertThat(kindField.schema.default).isEqualTo("sql_generation")
        assertThat(kindField.schema.values.orEmpty().map { it.value })
            .containsExactly("sql_generation", "tool_output", "general")

        val enabledField = manifest.payload.fields.orEmpty().first { it.name == "enabled" }
        assertThat(enabledField.schema.default).isEqualTo(true)
    }

    @Test
    fun shouldMirrorAiAnnotationInPlatformFacetTypesJson() {
        val jsonManifest = PlatformFacetTypeDefinitions.manifests()
            .single { it.typeKey == MetadataUrns.FACET_TYPE_AI_ANNOTATION }

        val yamlDef = readBootstrapDefinition()
        assertThat(jsonManifest.title).isEqualTo(yamlDef.displayName)
        assertThat(jsonManifest.category).isEqualTo(yamlDef.category)
        assertThat(jsonManifest.targetCardinality).isEqualTo(yamlDef.targetCardinality)
        assertThat(jsonManifest.applicableTo).isEqualTo(yamlDef.applicableTo)
        assertThat(jsonManifest.schemaVersion).isEqualTo(yamlDef.schemaVersion)
    }

    @Test
    fun shouldParseSkymillSegmentsAiAnnotationFixture() {
        val repoRoot = Path.of("").toAbsolutePath()
            .let { path ->
                generateSequence(path) { it.parent }
                    .firstOrNull { it.resolve("test/datasets/skymill/skymill-meta-seed-canonical.yaml").toFile().exists() }
                    ?: path
            }
        val seedFile = repoRoot.resolve("test/datasets/skymill/skymill-meta-seed-canonical.yaml")
        assertThat(seedFile).exists()

        val doc = MetadataYamlSerializer.deserialize(seedFile.toFile().readText())
        val segmentsUrn = "urn:mill/model/table:skymill.segments"
        assertThat(doc.entities.map { it.id }).contains(segmentsUrn)

        val facets = doc.facetsByEntity[segmentsUrn].orEmpty()
        val aiAnnotation = facets.single { it.facetTypeKey == MetadataUrns.FACET_TYPE_AI_ANNOTATION }
        val payload = aiAnnotation.payload
        assertThat(payload["title"]).isEqualTo("City name projection")
        assertThat(payload["instruction"].toString()).contains("skymill.cities")
        assertThat(payload["kind"]).isEqualTo("sql_generation")
    }

    private fun readBootstrapDefinition(): FacetTypeDefinition {
        val yaml = readSeedResource("metadata/platform-bootstrap.yaml")
        val doc = MetadataYamlSerializer.deserialize(yaml)
        return doc.definitions.single { it.typeKey == MetadataUrns.FACET_TYPE_AI_ANNOTATION }
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
            payload = payload,
        )
    }

    private fun readSeedResource(path: String): String {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: error("Missing classpath resource $path")
        return stream.bufferedReader().readText()
    }
}
