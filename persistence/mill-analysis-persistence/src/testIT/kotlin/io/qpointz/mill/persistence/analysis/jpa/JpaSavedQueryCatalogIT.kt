package io.qpointz.mill.persistence.analysis.jpa

import io.qpointz.mill.analysis.queries.SavedQuery
import io.qpointz.mill.persistence.analysis.jpa.adapters.JpaSavedQueryCatalog
import io.qpointz.mill.persistence.analysis.jpa.repositories.SavedQueryJpaRepository
import io.qpointz.mill.utils.JsonUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class JpaSavedQueryCatalogIT {

    @Autowired
    lateinit var repository: SavedQueryJpaRepository

    private val catalog by lazy { JpaSavedQueryCatalog(repository, JsonUtils.defaultJsonMapper()) }

    @Test
    fun shouldListSeededQueries_whenFlywayApplied() {
        val all = catalog.findAll()
        assertThat(all).hasSize(6)
        assertThat(all.map { it.id }).contains("top-customers", "daily-revenue")
        assertThat(all.first().sql).isNotBlank()
        assertThat(all.first().tags).isNotEmpty()
    }

    @Test
    fun shouldFindById_whenSeedExists() {
        val query = catalog.findById("top-customers")
        assertThat(query).isNotNull
        assertThat(query!!.name).isEqualTo("Top Customers by Revenue")
        assertThat(query.tags).containsExactly("revenue", "customer")
    }

    @Test
    fun shouldReturnNull_whenIdMissing() {
        assertThat(catalog.findById("missing-id")).isNull()
    }

    @Test
    fun shouldSaveUpdateAndDelete_whenMutationsRequested() {
        val now = Instant.parse("2025-03-01T12:00:00Z")
        val created = catalog.save(
            SavedQuery(
                id = "it-catalog-query",
                name = "Catalog IT",
                description = "temp",
                sql = "SELECT 1",
                createdAt = now,
                updatedAt = now,
                tags = listOf("it"),
            ),
        )
        assertThat(created.name).isEqualTo("Catalog IT")

        val updated = catalog.save(
            created.copy(sql = "SELECT 2", updatedAt = now.plusSeconds(60)),
        )
        assertThat(catalog.findById("it-catalog-query")!!.sql).isEqualTo("SELECT 2")

        assertThat(catalog.deleteById("it-catalog-query")).isTrue()
        assertThat(catalog.findById("it-catalog-query")).isNull()
        assertThat(updated.sql).isEqualTo("SELECT 2")
    }
}
