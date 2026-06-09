package io.qpointz.mill.analysis.queries.it

import io.qpointz.mill.analysis.queries.SavedQueryCatalog
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import tools.jackson.databind.json.JsonMapper

/**
 * Integration tests for {@code /api/v1/analysis/queries} backed by Flyway seeds and JPA.
 */
@SpringBootTest(
    classes = [TestAnalysisQueriesApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
)
@AutoConfigureMockMvc(addFilters = false)
class SavedQueriesRestIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var catalog: SavedQueryCatalog

    private val mapper = JsonMapper.builder().findAndAddModules().build()

    @Test
    fun shouldExposeSeededCatalog_whenFlywayApplied() {
        assertThat(catalog.findAll()).hasSize(6)
    }

    @Test
    fun shouldListSeededQueries_whenFlywayApplied() {
        val json = mockMvc.get("/api/v1/analysis/queries")
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString

        val root = mapper.readTree(json)
        val queries = root.get("queries")
        assertThat(queries.isArray).isTrue()
        assertThat(queries.size()).isEqualTo(6)
        val ids = (0 until queries.size()).map { queries[it].get("id").asText() }.toSet()
        assertThat(ids).contains("top-customers", "daily-revenue")
    }

    @Test
    fun shouldReturnQuery_whenIdExists() {
        mockMvc.get("/api/v1/analysis/queries/top-customers")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value("top-customers") }
                jsonPath("$.name") { value("Top Customers by Revenue") }
                jsonPath("$.sql") { exists() }
                jsonPath("$.tags[0]") { value("revenue") }
            }
    }

    @Test
    fun shouldReturn404_whenIdMissing() {
        mockMvc.get("/api/v1/analysis/queries/missing-id")
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun shouldCreateUpdateAndDeleteQuery_whenMutationsRequested() {
        mockMvc.post("/api/v1/analysis/queries") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":"it-temp-query","name":"IT Temp","sql":"SELECT 1"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value("it-temp-query") }
        }

        mockMvc.put("/api/v1/analysis/queries/it-temp-query") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"IT Temp Updated","sql":"SELECT 2"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.sql") { value("SELECT 2") }
        }

        mockMvc.delete("/api/v1/analysis/queries/it-temp-query")
            .andExpect { status { isNoContent() } }

        mockMvc.get("/api/v1/analysis/queries/it-temp-query")
            .andExpect { status { isNotFound() } }
    }
}
