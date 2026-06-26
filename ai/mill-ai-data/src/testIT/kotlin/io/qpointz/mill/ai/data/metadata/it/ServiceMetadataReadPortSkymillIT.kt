package io.qpointz.mill.ai.data.metadata.it

import io.qpointz.mill.ai.capabilities.metadata.MetadataReadPort
import io.qpointz.mill.ai.data.sql.it.SqlValidatorSkymillFlowItApplication
import io.qpointz.mill.metadata.domain.MetadataContent
import io.qpointz.mill.metadata.domain.MetadataUrns
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Verifies [MetadataReadPort] wiring against Skymill seeds (platform bootstrap + category guidance).
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [SqlValidatorSkymillFlowItApplication::class])
class ServiceMetadataReadPortSkymillIT {

  companion object {
    @JvmStatic
    @DynamicPropertySource
    fun metadataSeeds(registry: DynamicPropertyRegistry) {
      val root =
        System.getProperty("flow.facet.it.root")
          ?: error("System property 'flow.facet.it.root' not set (expected from Gradle testIT)")
      registry.add("mill.metadata.seed.resources[0]") { "classpath:metadata/platform-bootstrap.yaml" }
      registry.add("mill.metadata.seed.resources[1]") { "classpath:metadata/platform-flow-facet-types.yaml" }
      registry.add("mill.metadata.seed.resources[2]") { "classpath:metadata/platform-dq-l1-facet-types.yaml" }
      registry.add("mill.metadata.seed.resources[3]") { "classpath:metadata/platform-dq-l2-facet-types.yaml" }
      registry.add("mill.metadata.seed.resources[4]") { "classpath:metadata/platform-facet-category-guidance.yaml" }
      registry.add("mill.metadata.seed.resources[5]") { "classpath:metadata/platform-facet-authoring-examples.yaml" }
      registry.add("mill.metadata.seed.resources[6]") { "file:$root/test/datasets/skymill/skymill-canonical.yaml" }
      registry.add("mill.metadata.seed.resources[7]") { "file:$root/test/datasets/skymill/skymill-extras-seed.yaml" }
    }
  }

  @Autowired
  private lateinit var metadataReadPort: MetadataReadPort

  @Test
  fun shouldExposePlatformFacetTypes() {
    val keys = metadataReadPort.listFacetTypes().map { it.typeKey }
    assertThat(keys).contains(
      MetadataUrns.FACET_TYPE_DESCRIPTIVE,
      "urn:mill/metadata/facet-type:relation-source",
      "urn:mill/metadata/facet-type:dq-null-check",
      "urn:mill/metadata/facet-type:dq-predicate",
    )
  }

  @Test
  fun shouldExposeCategoryGuidance() {
    val categories = metadataReadPort.listFacetCategories().map { it.category }
    assertThat(categories).contains("general", "relation", "data-quality")
  }

  @Test
  fun shouldJoinExamplesOnGetFacetType() {
    val manifest = metadataReadPort.getFacetType("descriptive")
    assertThat(manifest).isNotNull
    val examples = metadataReadPort.listContent(
      manifest!!.typeKey,
      MetadataContent.KIND_FACET_TYPE_EXAMPLE,
    )
    assertThat(examples).isNotEmpty
  }
}
