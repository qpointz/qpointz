package io.qpointz.mill.ai.autoconfigure.sqlquery

import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.ai.autoconfigure.AiV3DataAutoConfiguration
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryExecutionPort
import io.qpointz.mill.data.query.engine.QueryResultExecutionService
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Wires [SqlQueryExecutionPort] to [QueryResultExecutionService] when the query-result engine is
 * available on the application classpath.
 */
@ConditionalOnAiEnabled
@AutoConfiguration(after = [AiV3DataAutoConfiguration::class])
@AutoConfigureAfter(name = ["io.qpointz.mill.autoconfigure.data.query.QueryResultEngineAutoConfiguration"])
@ConditionalOnBean(QueryResultExecutionService::class)
@EnableConfigurationProperties(SqlQueryExecutionProperties::class)
class AiV3SqlQueryExecutionAutoConfiguration {

    /**
     * @return resolver that maps Spring Security principals to query-result tenants
     */
    @Bean
    @ConditionalOnMissingBean(SqlQueryExecutionCallerContextResolver::class)
    fun sqlQueryExecutionCallerContextResolver(): SqlQueryExecutionCallerContextResolver =
        SecuritySqlQueryExecutionCallerContextResolver()

    /**
     * @param executionService in-process query-result engine
     * @param callerContextResolver tenant resolver for session ownership
     * @param objectMapper JSON parser for `rows-objects` pages
     * @return production [SqlQueryExecutionPort] adapter
     */
    @Bean
    @ConditionalOnMissingBean(SqlQueryExecutionPort::class)
    fun queryResultSqlQueryExecutionPort(
        executionService: QueryResultExecutionService,
        callerContextResolver: SqlQueryExecutionCallerContextResolver,
        objectMapper: ObjectProvider<ObjectMapper>,
    ): SqlQueryExecutionPort = QueryResultSqlQueryExecutionPort(
        executionService = executionService,
        callerContextResolver = callerContextResolver,
        objectMapper = objectMapper.getIfAvailable { ObjectMapper() },
    )
}
