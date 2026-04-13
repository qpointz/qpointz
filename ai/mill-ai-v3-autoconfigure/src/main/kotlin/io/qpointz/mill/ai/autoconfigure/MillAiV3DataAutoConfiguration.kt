package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.capabilities.schema.SchemaExplorationPort
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidator
import io.qpointz.mill.ai.data.schema.asSchemaExplorationPort
import io.qpointz.mill.ai.data.sql.EngineBackedSqlValidator
import io.qpointz.mill.data.schema.SchemaFacetService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Registers data-layer defaults for AI v3 when [SchemaFacetService] is available: a
 * [SchemaExplorationPort] and an [EngineBackedSqlValidator]. Hosts may override either bean.
 */
@AutoConfiguration
@ConditionalOnClass(SchemaFacetService::class, SchemaExplorationPort::class, EngineBackedSqlValidator::class)
class MillAiV3DataAutoConfiguration {

    /**
     * @param schemaFacetService merged physical + metadata schema boundary from Mill Data
     * @return port used by the `schema` capability tools
     */
    @Bean
    @ConditionalOnBean(SchemaFacetService::class)
    @ConditionalOnMissingBean(SchemaExplorationPort::class)
    fun schemaExplorationPort(schemaFacetService: SchemaFacetService): SchemaExplorationPort =
        schemaFacetService.asSchemaExplorationPort()

    /**
     * @param schemaFacetService used for catalog-aware SQL parsing validation
     * @return [SqlValidator] bean bridged to [io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.SqlValidationService]
     * via [MillAiV3SqlValidatorAutoConfiguration] when no custom validation service exists
     */
    @Bean
    @ConditionalOnBean(SchemaFacetService::class)
    @ConditionalOnMissingBean(SqlValidator::class)
    fun engineBackedSqlValidator(schemaFacetService: SchemaFacetService): SqlValidator =
        EngineBackedSqlValidator(schemaFacetService)
}
