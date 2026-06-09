package io.qpointz.mill.analysis.queries.web

import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [AnalysisDialectRestController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(AnalysisDialectRestControllerTest.DialectTestConfiguration::class)
class AnalysisDialectRestControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun shouldReturnConfiguredDialect_whenCalciteActive() {
        mvc.get("/api/v1/analysis/dialect")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value("CALCITE") }
                jsonPath("$.name") { value("Apache Calcite") }
                jsonPath("$.editorDialect") { value("standard") }
                jsonPath("$.identifiers.quoteStart") { value("`") }
                jsonPath("$.functions.strings[0]") { value("LENGTH") }
            }
    }

    class DialectTestConfiguration {
        @Bean
        fun sqlDialectSpec() = DialectRegistry.fromClasspathDefaults().requireDialect("CALCITE")
    }
}
