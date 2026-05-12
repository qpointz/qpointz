package io.qpointz.mill.data.query.config

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.data.query.MillDataQueryProperties
import io.qpointz.mill.data.query.engine.DefaultQueryResultExecutionService
import io.qpointz.mill.data.query.engine.QueryResultEngineSettings
import io.qpointz.mill.data.query.engine.QueryResultExecutionService
import io.qpointz.mill.data.query.engine.marshal.ResultMarshallerRegistry
import io.qpointz.mill.data.query.web.QueryResultExceptionHandler
import io.qpointz.mill.data.query.web.QueryResultRestController
import io.qpointz.mill.utils.JsonUtils
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import tools.jackson.databind.json.JsonMapper

/**
 * Registers query-result execution beans when {@code mill.data.services.query} is enabled.
 */
@AutoConfiguration
@ConditionalOnService(value = "query", group = "data")
@EnableConfigurationProperties(MillDataQueryProperties::class)
@Import(QueryResultRestController::class, QueryResultExceptionHandler::class)
class QueryResultWebAutoConfiguration {

    /**
     * @return shared JSON mapper for HTTP envelopes (falls back to Boot auto-config when present)
     */
    @Bean
    @ConditionalOnMissingBean(JsonMapper::class)
    fun queryResultJsonMapper(): JsonMapper = JsonUtils.defaultJsonMapper()

    /**
     * @return SPI-backed marshaller registry (built-ins from {@code mill-data-query}).
     */
    @Bean
    @ConditionalOnMissingBean
    fun resultMarshallerRegistry(): ResultMarshallerRegistry =
        ResultMarshallerRegistry.load()

    /**
     * @param dispatcher data-plane dispatcher from {@code DefaultServiceConfiguration}.
     * @param registry marshaller registry.
     * @param props engine tuning from {@code mill.data.query.*}.
     */
    @Bean
    @ConditionalOnBean(DataOperationDispatcher::class)
    fun queryResultExecutionService(
        dispatcher: DataOperationDispatcher,
        registry: ResultMarshallerRegistry,
        props: MillDataQueryProperties,
    ): QueryResultExecutionService {
        val settings = QueryResultEngineSettings(
            maxMaterializedRows = props.maxMaterializedRows,
            sessionExpireAfterAccess = props.sessionExpireAfterAccess,
            defaultFetchSize = props.defaultFetchSize,
            maxPageSize = props.maxPageSize,
        )
        return DefaultQueryResultExecutionService(dispatcher, registry, settings)
    }
}
