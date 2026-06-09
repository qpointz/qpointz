package io.qpointz.mill.analysis.queries.it

import io.qpointz.mill.analysis.queries.web.AnalysisDialectRestController
import io.qpointz.mill.analysis.queries.web.SavedQueriesRestController
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.persistence.analysis.jpa.AnalysisPersistenceAutoConfiguration
import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

/**
 * Minimal servlet application for Analysis REST integration tests.
 */
@SpringBootApplication
@Import(
    AnalysisPersistenceAutoConfiguration::class,
    SavedQueriesRestController::class,
    AnalysisDialectRestController::class,
)
class TestAnalysisQueriesApplication {

    /**
     * Satisfies {@code @ConditionalOnBean(DataOperationDispatcher)} for analysis queries autoconfig.
     */
    @Bean
    fun dataOperationDispatcher(): DataOperationDispatcher = mock()

    /**
     * Configured dialect for {@link AnalysisDialectRestController} tests.
     */
    @Bean
    fun sqlDialectSpec(): SqlDialectSpec = DialectRegistry.fromClasspathDefaults().requireDialect("CALCITE")
}
