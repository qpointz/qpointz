package io.qpointz.mill.analysis.queries.it

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Verifies Analysis REST is registered through auto-configuration (mill-service wiring path).
 */
@SpringBootTest(
    classes = [TestAnalysisQueriesApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
)
@AutoConfigureMockMvc(addFilters = false)
class AnalysisQueriesWebAutoConfigurationIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun shouldExposeSavedQueryCatalog_whenAutoConfigurationWired() {
        mockMvc.get("/api/v1/analysis/queries")
            .andExpect {
                status { isOk() }
                jsonPath("$.queries.length()") { value(6) }
            }
    }

    @Test
    fun shouldExposeDialect_whenAutoConfigurationWired() {
        mockMvc.get("/api/v1/analysis/dialect")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value("CALCITE") }
            }
    }
}
