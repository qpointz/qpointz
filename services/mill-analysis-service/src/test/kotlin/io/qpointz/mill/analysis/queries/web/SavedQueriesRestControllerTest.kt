package io.qpointz.mill.analysis.queries.web

import io.qpointz.mill.analysis.queries.SavedQuery
import io.qpointz.mill.analysis.queries.SavedQueryCatalog
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import java.time.Instant

@WebMvcTest(controllers = [SavedQueriesRestController::class])
@AutoConfigureMockMvc(addFilters = false)
class SavedQueriesRestControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockitoBean
    private lateinit var catalog: SavedQueryCatalog

    private val sample = SavedQuery(
        id = "top-customers",
        name = "Top Customers by Revenue",
        description = "Customers ranked by total order value",
        sql = "SELECT 1",
        createdAt = Instant.parse("2025-01-28T12:00:00Z"),
        updatedAt = Instant.parse("2025-02-08T12:00:00Z"),
        tags = listOf("revenue", "customer"),
    )

    @Test
    fun shouldReturnQueryList_whenCatalogHasEntries() {
        whenever(catalog.findAll()).thenReturn(listOf(sample))

        mvc.get("/api/v1/analysis/queries")
            .andExpect {
                status { isOk() }
                jsonPath("$.queries.length()") { value(1) }
                jsonPath("$.queries[0].id") { value("top-customers") }
                jsonPath("$.queries[0].createdAt") { value(sample.createdAt.toEpochMilli()) }
                jsonPath("$.queries[0].tags[0]") { value("revenue") }
            }
    }

    @Test
    fun shouldReturnEmptyList_whenCatalogEmpty() {
        whenever(catalog.findAll()).thenReturn(emptyList())

        mvc.get("/api/v1/analysis/queries")
            .andExpect {
                status { isOk() }
                jsonPath("$.queries.length()") { value(0) }
            }
    }

    @Test
    fun shouldReturnQuery_whenIdExists() {
        whenever(catalog.findById("top-customers")).thenReturn(sample)

        mvc.get("/api/v1/analysis/queries/top-customers")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value("top-customers") }
                jsonPath("$.sql") { value("SELECT 1") }
            }
    }

    @Test
    fun shouldReturn404_whenIdMissing() {
        whenever(catalog.findById("missing")).thenReturn(null)

        mvc.get("/api/v1/analysis/queries/missing")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun shouldCreateQuery_whenIdAvailable() {
        whenever(catalog.findById("new-query")).thenReturn(null)
        whenever(catalog.findAll()).thenReturn(emptyList())
        whenever(catalog.save(any())).thenAnswer { it.arguments[0] as SavedQuery }

        mvc.post("/api/v1/analysis/queries") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":"new-query","name":"New Query","sql":"SELECT 1"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value("new-query") }
            jsonPath("$.name") { value("New Query") }
        }

        verify(catalog).save(any())
    }

    @Test
    fun shouldUpdateQuery_whenIdExists() {
        whenever(catalog.findById("top-customers")).thenReturn(sample)
        whenever(catalog.save(any())).thenAnswer { it.arguments[0] as SavedQuery }

        mvc.put("/api/v1/analysis/queries/top-customers") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Updated","sql":"SELECT 2"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Updated") }
            jsonPath("$.sql") { value("SELECT 2") }
        }
    }

    @Test
    fun shouldDeleteQuery_whenIdExists() {
        whenever(catalog.deleteById("top-customers")).thenReturn(true)

        mvc.delete("/api/v1/analysis/queries/top-customers")
            .andExpect { status { isNoContent() } }
    }

    @Test
    fun shouldReturn404_whenDeleteMissing() {
        whenever(catalog.deleteById("missing")).thenReturn(false)

        mvc.delete("/api/v1/analysis/queries/missing")
            .andExpect { status { isNotFound() } }
    }
}
