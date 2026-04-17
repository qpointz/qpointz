package io.qpointz.mill.ai.data.schema.it

import io.qpointz.mill.ai.capabilities.schema.RelationDirection
import io.qpointz.mill.ai.data.schema.asSchemaCatalogPort
import io.qpointz.mill.ai.data.sql.it.SqlValidatorSkymillFlowItApplication
import io.qpointz.mill.data.schema.SchemaFacetService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Integration test for [io.qpointz.mill.ai.data.schema.SchemaFacetCatalogAdapter] against the same
 * Skymill flow backend as SQL validator IT, with metadata seeds aligned to
 * `apps/mill-service/application.yml` profile `skymill` (canonical + extras YAML under
 * `test/datasets/skymill/`).
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [SqlValidatorSkymillFlowItApplication::class])
class SchemaFacetCatalogSkymillCanonicalIT {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun skymillCanonicalSeeds(registry: DynamicPropertyRegistry) {
            val root =
                System.getProperty("flow.facet.it.root")
                    ?: error("System property 'flow.facet.it.root' not set (expected from Gradle testIT)")
            registry.add("mill.metadata.seed.resources[0]") { "classpath:metadata/platform-bootstrap.yaml" }
            registry.add("mill.metadata.seed.resources[1]") { "classpath:metadata/platform-flow-facet-types.yaml" }
            registry.add("mill.metadata.seed.resources[2]") { "file:$root/test/datasets/skymill/skymill-canonical.yaml" }
            registry.add("mill.metadata.seed.resources[3]") { "file:$root/test/datasets/skymill/skymill-extras-seed.yaml" }
        }
    }

    @Autowired
    private lateinit var schemaFacetService: SchemaFacetService

    @Test
    fun `model root relations surface for aircraft outbound`() {
        val catalog = schemaFacetService.asSchemaCatalogPort()
        val rels = catalog.listRelations("skymill", "aircraft", RelationDirection.OUTBOUND)
        assertThat(rels.any { it.targetTable == "cargo_flights" && it.joinSql.contains("aircraft_id") })
            .isTrue()
    }

    @Test
    fun `descriptive display names are exposed on list tables`() {
        val catalog = schemaFacetService.asSchemaCatalogPort()
        val cities = catalog.listTables("skymill").find { it.tableName == "cities" }
        assertThat(cities).isNotNull
        assertThat(cities!!.displayName).isEqualTo("Cities")
    }
}
