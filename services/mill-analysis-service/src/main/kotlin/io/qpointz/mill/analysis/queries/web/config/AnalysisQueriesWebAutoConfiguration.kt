package io.qpointz.mill.analysis.queries.web.config

import io.qpointz.mill.analysis.queries.SavedQueryCatalog
import io.qpointz.mill.analysis.queries.web.AnalysisDialectRestController
import io.qpointz.mill.analysis.queries.web.SavedQueriesRestController
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Import

/**
 * Registers Analysis REST MVC when the data plane, catalog port, and dialect are available.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(DataOperationDispatcher::class, SavedQueryCatalog::class, SqlDialectSpec::class)
@Import(SavedQueriesRestController::class, AnalysisDialectRestController::class)
class AnalysisQueriesWebAutoConfiguration
