package io.qpointz.mill.ai.data.sql.it

import io.qpointz.mill.ai.data.sql.EngineBackedSqlValidator
import io.qpointz.mill.data.schema.SchemaFacetService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Path

/**
 * Parse + schema-bound validation against the shared Skymill + flow fixture.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [SqlValidatorSkyMillFlowTestApplication::class])
class EngineBackedSqlValidatorSkyMillFlowIT {

    @Autowired
    private lateinit var schemaFacetService: SchemaFacetService

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun flowSkymillProperties(registry: DynamicPropertyRegistry) {
            val root =
                System.getProperty("flow.facet.it.root")
                    ?: error("System property 'flow.facet.it.root' not set")
            val yamlPath =
                Path.of(root, "data", "mill-data-backends", "config", "test", "flow-skymill.yaml")
            registry.add("mill.data.backend.type") { "flow" }
            registry.add("mill.data.backend.flow.sources[0]") { yamlPath.toString() }
            registry.add("mill.metadata.repository.type") { "file" }
            registry.add("mill.metadata.seed.resources[0]") { "classpath:metadata/platform-bootstrap.yaml" }
            registry.add("mill.metadata.seed.resources[1]") { "classpath:metadata/platform-flow-facet-types.yaml" }
        }
    }

    @Test
    fun `accepts select on skymill cities`() {
        val validator = EngineBackedSqlValidator(schemaFacetService)
        val outcome =
            validator.validate(
                "SELECT id, city FROM skymill.cities FETCH FIRST 1 ROWS ONLY",
            )
        assertThat(outcome.passed).isTrue()
    }

    @Test
    fun `rejects unknown table`() {
        val validator = EngineBackedSqlValidator(schemaFacetService)
        val outcome =
            validator.validate(
                "SELECT 1 FROM skymill.nonexistent_table",
            )
        assertThat(outcome.passed).isFalse()
    }
}
