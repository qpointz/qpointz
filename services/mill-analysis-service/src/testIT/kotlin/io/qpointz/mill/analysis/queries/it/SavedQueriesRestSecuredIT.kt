package io.qpointz.mill.analysis.queries.it

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user

/**
 * Auth behaviour for saved-query catalog when API security requires authentication (WI-260).
 */
@SpringBootTest(
    classes = [TestAnalysisQueriesApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
)
@AutoConfigureMockMvc
@Import(SecuredApiTestConfiguration::class)
class SavedQueriesRestSecuredIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun shouldReturn403_whenUnauthenticated() {
        mockMvc.get("/api/v1/analysis/queries")
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun shouldReturnCatalog_whenAuthenticated() {
        mockMvc.get("/api/v1/analysis/queries") { with(user("alice")) }
            .andExpect {
                status { isOk() }
                jsonPath("$.queries.length()") { value(6) }
            }
    }
}
