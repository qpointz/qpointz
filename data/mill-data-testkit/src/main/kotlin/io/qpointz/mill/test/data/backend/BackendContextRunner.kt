package io.qpointz.mill.test.data.backend

import io.qpointz.mill.data.backend.ExecutionProvider
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.backend.SqlProvider
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter
import io.qpointz.mill.security.NoneSecurityProvider
import io.qpointz.mill.security.SecurityProvider
import java.util.function.Consumer

abstract class BackendContextRunner(
    private val calciteContextFactoryOverride: CalciteContextFactory? = null,
    private val executionProviderOverride: ExecutionProvider? = null,
    private val schemaProviderOverride: SchemaProvider? = null,
    private val sqlProviderOverride: SqlProvider? = null,
    private val planConverterOverride: PlanConverter? = null,
    private val securityProviderOverride: SecurityProvider? = null,
) {

    protected abstract fun buildCalciteContextFactory(): CalciteContextFactory
    protected abstract fun buildExecutionProvider(): ExecutionProvider
    protected abstract fun buildSchemaProvider(): SchemaProvider
    protected abstract fun buildSqlProvider(): SqlProvider
    protected abstract fun buildPlanConverter(): PlanConverter
    protected open fun buildSecurityProvider(): SecurityProvider = NoneSecurityProvider()

    val calciteContextFactory: CalciteContextFactory by lazy { calciteContextFactoryOverride ?: buildCalciteContextFactory() }
    val executionProvider: ExecutionProvider by lazy { executionProviderOverride ?: buildExecutionProvider() }
    val schemaProvider: SchemaProvider by lazy { schemaProviderOverride ?: buildSchemaProvider() }
    val sqlProvider: SqlProvider by lazy { sqlProviderOverride ?: buildSqlProvider() }
    val planConverter: PlanConverter by lazy { planConverterOverride ?: buildPlanConverter() }
    val securityProvider: SecurityProvider by lazy { securityProviderOverride ?: buildSecurityProvider() }

    protected abstract fun derive(
        calciteContextFactoryOverride: CalciteContextFactory? = this.calciteContextFactoryOverride,
        executionProviderOverride: ExecutionProvider? = this.executionProviderOverride,
        schemaProviderOverride: SchemaProvider? = this.schemaProviderOverride,
        sqlProviderOverride: SqlProvider? = this.sqlProviderOverride,
        planConverterOverride: PlanConverter? = this.planConverterOverride,
        securityProviderOverride: SecurityProvider? = this.securityProviderOverride,
    ): BackendContextRunner

    fun withCalciteContextFactory(cf: CalciteContextFactory) = derive(calciteContextFactoryOverride = cf)

    fun withExecution(ep: ExecutionProvider) = derive(executionProviderOverride = ep)

    fun withSchema(sp: SchemaProvider) = derive(schemaProviderOverride = sp)

    fun withSql(sp: SqlProvider) = derive(sqlProviderOverride = sp)

    fun withPlanConverter(pc: PlanConverter) = derive(planConverterOverride = pc)

    fun withSecurity(sp: SecurityProvider) = derive(securityProviderOverride = sp)

    fun run(cns: Consumer<BackendContextRunner>) {
        cns.accept(this)
    }

}
