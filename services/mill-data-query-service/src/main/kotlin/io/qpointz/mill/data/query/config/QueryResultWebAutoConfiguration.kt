package io.qpointz.mill.data.query.config

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.autoconfigure.data.query.QueryResultEngineAutoConfiguration
import io.qpointz.mill.data.query.engine.QueryResultExecutionService
import io.qpointz.mill.data.query.web.QueryResultExceptionHandler
import io.qpointz.mill.data.query.web.QueryResultRestController
import io.qpointz.mill.utils.JsonUtils
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import tools.jackson.databind.json.JsonMapper

/**
 * Registers MVC query-result controllers when the {@code query} data-plane service is enabled.
 *
 * <p>Depends on [QueryResultEngineAutoConfiguration] for [QueryResultExecutionService]; use that
 * auto-configuration (from {@code mill-data-autoconfigure}) without this module for in-process-only use.
 */
@AutoConfiguration(after = [QueryResultEngineAutoConfiguration::class])
@ConditionalOnService(value = "query", group = "data")
@ConditionalOnBean(QueryResultExecutionService::class)
@Import(QueryResultRestController::class, QueryResultExceptionHandler::class)
class QueryResultWebAutoConfiguration {

    /**
     * @return shared JSON mapper for HTTP envelopes (falls back to Boot auto-config when present)
     */
    @Bean
    @ConditionalOnMissingBean(JsonMapper::class)
    fun queryResultJsonMapper(): JsonMapper = JsonUtils.defaultJsonMapper()
}
