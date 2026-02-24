package io.qpointz.mill.test.data.backend

import io.qpointz.mill.data.backend.ExecutionProvider
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.backend.SqlProvider
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory
import io.qpointz.mill.data.backend.calcite.CalciteSqlDialectConventions
import io.qpointz.mill.data.backend.calcite.ConnectionContextFactory
import io.qpointz.mill.data.backend.calcite.providers.CalciteExecutionProvider
import io.qpointz.mill.data.backend.calcite.providers.CalcitePlanConverter
import io.qpointz.mill.data.backend.calcite.providers.CalciteSchemaProvider
import io.qpointz.mill.data.backend.calcite.providers.CalciteSqlProvider
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher
import io.qpointz.mill.sql.dialect.SqlDialectSpec
import io.qpointz.mill.sql.dialect.SqlDialectSpecs
import io.qpointz.mill.security.SecurityProvider
import io.substrait.extension.ExtensionCollector
import io.substrait.extension.SimpleExtension
import java.util.*

class CalciteBackendContextRunner(
    private val defaultCalciteContextFactory: CalciteContextFactory,
    private val sqlDialectConventions: CalciteSqlDialectConventions,
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
            sqlDialectConventions.sqlDialect(),
            extensionCollection
        )
    }

    override fun buildExecutionProvider(): ExecutionProvider {
        return CalciteExecutionProvider(this.calciteContextFactory, this.planConverter)
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
    ): CalciteBackendContextRunner {
        return CalciteBackendContextRunner(
            defaultCalciteContextFactory,
            sqlDialectConventions,
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
        fun calciteContext(
            modelPath: String,
            dialect: SqlDialectSpec = SqlDialectSpecs.CALCITE,
            conventionOverrides: Map<String, Any?> = emptyMap(),
            extensionCollection: SimpleExtension.ExtensionCollection? = SimpleExtension.loadDefaults()
        ): CalciteBackendContextRunner {
            val sqlDialectConventions = CalciteSqlDialectConventions(dialect)
            val conventionMap = sqlDialectConventions.asMap(conventionOverrides)

            val allProps = HashMap<String?, Any?>(conventionMap)
            allProps["model"] = modelPath

            val props = Properties()
            props.putAll(allProps)

            val calciteContextFactory = ConnectionContextFactory(props)

            val extCollection = extensionCollection ?: SimpleExtension.loadDefaults()
            val extensionCollector = ExtensionCollector()
            val substraitDispatcher = SubstraitDispatcher(extCollection)

            return CalciteBackendContextRunner(
                calciteContextFactory,
                sqlDialectConventions,
                extCollection,
                extensionCollector,
                substraitDispatcher,
            )
        }
    }

}
