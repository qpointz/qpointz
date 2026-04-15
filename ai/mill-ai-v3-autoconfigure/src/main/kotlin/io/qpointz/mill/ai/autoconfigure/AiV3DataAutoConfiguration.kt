package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.capabilities.schema.EmptySchemaCatalogPort
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidator
import io.qpointz.mill.ai.data.schema.asSchemaCatalogPort
import io.qpointz.mill.ai.data.sql.BackendSqlValidator
import io.qpointz.mill.data.backend.SqlProvider
import io.qpointz.mill.data.schema.SchemaFacetService
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(AiV3DataAutoConfiguration::class.java)

/**
 * Registers data-layer defaults for AI v3: a [SchemaCatalogPort] from [SchemaFacetService] when
 * present, otherwise an empty catalog so `schema`-capable profiles still satisfy dependency checks.
 * When a [SqlProvider] exists, registers [BackendSqlValidator]. Hosts may override any bean.
 *
 * **Order:** must run **after** Mill Data backend auto-configurations that register [SqlProvider];
 * otherwise [backendSqlValidator] is skipped when `@ConditionalOnBean(SqlProvider::class)` is
 * evaluated too early, and `sql-query` / `schema-authoring` chats fail dependency validation.
 *
 * Must also run **after** [SchemaFacetServiceAutoConfiguration] so [SchemaFacetService] is registered
 * before [schemaCatalogPort] is built (see [ObjectProvider] usage there).
 */
@ConditionalOnAiEnabled
@AutoConfiguration
@AutoConfigureAfter(
    name = [
        "io.qpointz.mill.autoconfigure.data.backend.calcite.CalciteBackendAutoConfiguration",
        "io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration",
        "io.qpointz.mill.autoconfigure.data.backend.jdbc.JdbcBackendAutoConfiguration",
        "io.qpointz.mill.autoconfigure.data.schema.SchemaFacetServiceAutoConfiguration",
    ],
)
@ConditionalOnClass(SchemaFacetService::class, SchemaCatalogPort::class, BackendSqlValidator::class)
class AiV3DataAutoConfiguration {

    /**
     * Port used by the `schema` capability tools: backed by [SchemaFacetService] when that bean exists,
     * otherwise a shared empty catalog so dependency wiring still succeeds.
     *
     * Uses [ObjectProvider] so a single factory runs after [SchemaFacetService] registration without
     * relying on declaration order of multiple `@Bean` methods (which could register the empty
     * catalog first and block the real port).
     *
     * @param schemaFacetService optional Mill Data facet aggregate (present when data auto-config runs first)
     * @return catalog port for AI tools
     */
    @Bean
    @ConditionalOnMissingBean(SchemaCatalogPort::class)
    fun schemaCatalogPort(schemaFacetService: ObjectProvider<SchemaFacetService>): SchemaCatalogPort {
        val facet = schemaFacetService.ifAvailable
        return if (facet != null) {
            facet.asSchemaCatalogPort()
        } else {
            log.warn(
                "AI v3: no SchemaFacetService-backed SchemaCatalogPort; using empty catalog. " +
                    "Schema tools will return no rows until Mill Data metadata/schema wiring is complete.",
            )
            EmptySchemaCatalogPort
        }
    }

    /**
     * @param sqlProvider Mill data backend SQL parser (e.g. flow / Calcite)
     * @return [SqlValidator] bean bridged to [io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.SqlValidationService]
     * via [AiV3SqlValidatorAutoConfiguration] when no custom validation service exists
     */
    @Bean
    @ConditionalOnBean(SqlProvider::class)
    @ConditionalOnMissingBean(SqlValidator::class)
    fun backendSqlValidator(sqlProvider: SqlProvider): SqlValidator =
        BackendSqlValidator(sqlProvider)
}
