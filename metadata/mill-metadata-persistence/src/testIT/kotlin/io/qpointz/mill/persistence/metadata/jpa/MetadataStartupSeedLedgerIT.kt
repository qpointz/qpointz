package io.qpointz.mill.persistence.metadata.jpa

import io.qpointz.mill.metadata.configuration.MetadataSeedStartup
import io.qpointz.mill.metadata.repository.EntityRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import io.qpointz.mill.persistence.metadata.jpa.it.MetadataSeedLedgerITApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

/**
 * Ledger-backed seeds: a second [MetadataSeedStartup.run] must not add duplicate entities (WI-126).
 */
@SpringBootTest(classes = [MetadataSeedLedgerITApplication::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = [
        "mill.metadata.repository.type=jpa",
        "mill.metadata.seed.resources[0]=classpath:metadata/platform-bootstrap.yaml",
        "mill.metadata.seed.resources[1]=classpath:metadata-seed/one-entity.yaml"
    ]
)
class MetadataStartupSeedLedgerIT {

    @Autowired
    private lateinit var entityRepository: EntityRepository

    @Autowired
    private lateinit var metadataSeedStartup: MetadataSeedStartup

    @Test
    fun `second seed run does not duplicate entities`() {
        val id = "urn:mill/metadata/entity:seed.startup.one"
        assertThat(entityRepository.exists(id)).isTrue
        val countAfterContextStart = entityRepository.findAll().size
        metadataSeedStartup.run()
        assertThat(entityRepository.findAll()).hasSize(countAfterContextStart)
        assertThat(entityRepository.exists(id)).isTrue
    }
}
