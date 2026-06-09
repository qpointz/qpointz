package io.qpointz.mill.analysis.queries.it

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Integration tests for {@code GET /api/v1/analysis/dialect}.
 */
@SpringBootTest(
    classes = [TestAnalysisQueriesApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
)
@AutoConfigureMockMvc(addFilters = false)
class AnalysisDialectRestIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun shouldReturnDialect_whenConfigured() {
        mockMvc.get("/api/v1/analysis/dialect")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value("CALCITE") }
                jsonPath("$.editorDialect") { value("standard") }
                jsonPath("$.functions.aggregates[0]") { value("COUNT") }
            }
    }
}
