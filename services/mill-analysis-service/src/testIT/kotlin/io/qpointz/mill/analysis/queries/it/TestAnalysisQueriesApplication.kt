package io.qpointz.mill.analysis.queries.it

import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

/**
 * Minimal servlet application for Analysis REST integration tests.
 *
 * Controllers are registered only via [io.qpointz.mill.analysis.queries.web.config.AnalysisQueriesWebAutoConfiguration]
 * (not direct {@code @Import}), matching mill-service startup.
 */
@SpringBootApplication
class TestAnalysisQueriesApplication {

    /**
     * Satisfies {@code @ConditionalOnBean(DataOperationDispatcher)} for analysis queries autoconfig.
     */
    @Bean
    fun dataOperationDispatcher(): DataOperationDispatcher = mock()

    /**
     * Configured dialect for {@link io.qpointz.mill.analysis.queries.web.AnalysisDialectRestController} tests.
     */
    @Bean
    fun sqlDialectSpec(): SqlDialectSpec = DialectRegistry.fromClasspathDefaults().requireDialect("CALCITE")
}
