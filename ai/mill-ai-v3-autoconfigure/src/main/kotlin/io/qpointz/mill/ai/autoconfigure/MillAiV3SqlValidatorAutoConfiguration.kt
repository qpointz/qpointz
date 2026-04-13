package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidator
import io.qpointz.mill.ai.capabilities.sqlquery.asSqlValidationService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Registers a [SqlQueryToolHandlers.SqlValidationService] bean when the application supplies a
 * [SqlValidator] and no custom [SqlQueryToolHandlers.SqlValidationService] is already defined.
 *
 * Chat runtimes and CLIs can then build [io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityDependency]
 * from the handler bean or from [SqlValidator] directly.
 */
@AutoConfiguration(after = [MillAiV3DataAutoConfiguration::class])
@ConditionalOnClass(SqlValidator::class)
class MillAiV3SqlValidatorAutoConfiguration {

    /**
     * @param validator application-provided SQL validation contract
     * @return adapter suitable for the `sql-query` capability
     */
    @Bean
    @ConditionalOnBean(SqlValidator::class)
    @ConditionalOnMissingBean(SqlQueryToolHandlers.SqlValidationService::class)
    fun sqlValidationServiceFromSqlValidator(
        validator: SqlValidator,
    ): SqlQueryToolHandlers.SqlValidationService = validator.asSqlValidationService()
}
