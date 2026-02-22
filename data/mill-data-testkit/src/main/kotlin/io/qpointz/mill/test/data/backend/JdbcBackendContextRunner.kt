package io.qpointz.mill.test.data.backend

import io.qpointz.mill.data.backend.ExecutionProvider
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.backend.SecurityProvider
import io.qpointz.mill.data.backend.SqlProvider
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory
import io.qpointz.mill.data.backend.calcite.CalciteSqlDialectConventions
import io.qpointz.mill.data.backend.calcite.providers.CalcitePlanConverter
import io.qpointz.mill.data.backend.calcite.providers.CalciteSchemaProvider
import io.qpointz.mill.data.backend.calcite.providers.CalciteSqlProvider
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher
import io.qpointz.mill.data.backend.jdbc.JdbcCalciteConfiguration
import io.qpointz.mill.data.backend.jdbc.providers.JdbcCalciteContextFactory
import io.qpointz.mill.data.backend.jdbc.providers.JdbcConnectionProvider
import io.qpointz.mill.data.backend.jdbc.providers.JdbcContextFactory
import io.qpointz.mill.data.backend.jdbc.providers.JdbcExecutionProvider
import io.qpointz.mill.data.backend.jdbc.providers.impl.JdbcConnectionCustomizerImpl
import io.qpointz.mill.data.backend.jdbc.providers.impl.JdbcContextFactoryImpl
import io.qpointz.mill.sql.dialect.SqlDialectSpec
import io.qpointz.mill.sql.dialect.SqlDialectSpecs
import io.substrait.extension.ExtensionCollector
import io.substrait.extension.SimpleExtension
import org.apache.calcite.sql.SqlDialect
import java.util.*

class JdbcBackendContextRunner(
    private val defaultCalciteContextFactory: CalciteContextFactory,
    val jdbcContextFactory: JdbcContextFactory,
    private val extensionCollection: SimpleExtension.ExtensionCollection,
    private val extensionCollector: ExtensionCollector,
    private val substraitDispatcher: SubstraitDispatcher,
    calciteContextFactoryOverride: CalciteContextFactory? = null,
    executionProviderOverride: ExecutionProvider? = null,
    schemaProviderOverride: SchemaProvider? = null,
    sqlProviderOverride: SqlProvider? = null,
    planConverterOverride: PlanConverter? = null,
    securityProviderOverride: SecurityProvider? = null,
) : BackendContextRunner(
    calciteContextFactoryOverride,
    executionProviderOverride,
    schemaProviderOverride,
    sqlProviderOverride,
    planConverterOverride,
    securityProviderOverride,
) {

    override fun buildCalciteContextFactory(): CalciteContextFactory {
        return defaultCalciteContextFactory
    }

    override fun buildPlanConverter(): PlanConverter {
        return CalcitePlanConverter(
            this.calciteContextFactory,
            SqlDialect.DatabaseProduct.CALCITE.dialect,
            extensionCollection
        )
    }

    override fun buildExecutionProvider(): ExecutionProvider {
        return JdbcExecutionProvider(this.planConverter, jdbcContextFactory)
    }

    override fun buildSchemaProvider(): SchemaProvider {
        return CalciteSchemaProvider(this.calciteContextFactory, extensionCollector)
    }

    override fun buildSqlProvider(): SqlProvider {
        return CalciteSqlProvider(this.calciteContextFactory, substraitDispatcher)
    }

    override fun derive(
        calciteContextFactoryOverride: CalciteContextFactory?,
        executionProviderOverride: ExecutionProvider?,
        schemaProviderOverride: SchemaProvider?,
        sqlProviderOverride: SqlProvider?,
        planConverterOverride: PlanConverter?,
        securityProviderOverride: SecurityProvider?,
    ): JdbcBackendContextRunner {
        return JdbcBackendContextRunner(
            defaultCalciteContextFactory,
            jdbcContextFactory,
            extensionCollection,
            extensionCollector,
            substraitDispatcher,
            calciteContextFactoryOverride,
            executionProviderOverride,
            schemaProviderOverride,
            sqlProviderOverride,
            planConverterOverride,
            securityProviderOverride,
        )
    }

    companion object {

        @JvmStatic
        fun jdbcBackendContext(
            dialect: SqlDialectSpec,
            url: String,
            driver: String,
            sqlConventions: Map<String, Any?>? = emptyMap(),
            user: String? = null,
            password: String? = null,
            catalog: String? = null,
            schema: String? = null,
            targetSchema: String? = null,
            multiSchema: Boolean? = false,
            extensionCollection: SimpleExtension.ExtensionCollection? = SimpleExtension.loadDefaults()
        ): JdbcBackendContextRunner {
            val calciteCfg = JdbcCalciteConfiguration.builder()
                .url(url)
                .driver(driver)
                .user(Optional.ofNullable(user))
                .password(Optional.ofNullable(password))
                .catalog(Optional.ofNullable(catalog))
                .schema(Optional.ofNullable(schema))
                .targetSchema(Optional.ofNullable(targetSchema))
                .multiSchema(multiSchema ?: false)
                .build()

            val connectionProvider: JdbcConnectionProvider = JdbcConnectionCustomizerImpl()

            val sqlDialectConventions = CalciteSqlDialectConventions(dialect)
            val conventionMap = sqlDialectConventions.asMap(sqlConventions)
            val props = Properties()
            props.putAll(conventionMap)

            val calciteContextFactory = JdbcCalciteContextFactory(props, calciteCfg, connectionProvider)
            val jdbcContextFactory = JdbcContextFactoryImpl(calciteCfg, connectionProvider)

            val extCollection = extensionCollection ?: SimpleExtension.loadDefaults()
            val extensionCollector = ExtensionCollector()
            val substraitDispatcher = SubstraitDispatcher(extCollection)

            return JdbcBackendContextRunner(
                calciteContextFactory,
                jdbcContextFactory,
                extCollection,
                extensionCollector,
                substraitDispatcher,
            )
        }

        @JvmStatic
        fun jdbcH2Context(
            url: String,
            targetSchema: String,
            catalog: String? = null,
            schema: String? = null,
            multiSchema: Boolean? = false
        ): JdbcBackendContextRunner {
            val sqlConventions = mapOf<String, Any?>(
                "caseSensitive" to true,
                "quoting" to "BACK_TICK",
                "unquotedCasing" to "UNCHANGED"
            )
            return jdbcBackendContext(
                dialect = SqlDialectSpecs.H2,
                url = url,
                driver = "org.h2.Driver",
                sqlConventions = sqlConventions,
                targetSchema = targetSchema,
                catalog = catalog,
                schema = schema,
                multiSchema = multiSchema ?: false
            )
        }
    }

}
