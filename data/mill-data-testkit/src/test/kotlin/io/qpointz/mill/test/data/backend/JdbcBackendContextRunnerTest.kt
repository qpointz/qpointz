package io.qpointz.mill.test.data.backend

import io.qpointz.mill.data.backend.ExecutionProvider
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.backend.SqlProvider
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter
import io.qpointz.mill.security.NoneSecurityProvider
import io.qpointz.mill.security.SecurityProvider
import io.qpointz.mill.proto.QueryExecutionConfig
import io.substrait.plan.ImmutablePlan
import io.substrait.plan.Plan
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.stream.StreamSupport

@ExtendWith(MockitoExtension::class)
class JdbcBackendContextRunnerTest {

    private val runner = JdbcBackendContextRunner.jdbcH2Context(
        url = "jdbc:h2:mem:testkit-jdbc;INIT=RUNSCRIPT FROM './config/test/testdata.sql'",
        targetSchema = "ts"
    )

    @Test
    fun shouldResolveAllProviders() {
        assertNotNull(runner.executionProvider)
        assertNotNull(runner.schemaProvider)
        assertNotNull(runner.sqlProvider)
        assertNotNull(runner.planConverter)
        assertNotNull(runner.securityProvider)
    }

    @Test
    fun shouldReturnSchemaNames() {
        val schemas = StreamSupport.stream(runner.schemaProvider.schemaNames.spliterator(), false)
            .toList()
        assertTrue(schemas.isNotEmpty())
    }

    @Test
    fun shouldDefaultToNoneSecurityProvider() {
        assertTrue(runner.securityProvider is NoneSecurityProvider)
    }

    @Test
    fun shouldOverrideExecutionProvider(@Mock mockExec: ExecutionProvider) {
        val mutated = runner.withExecution(mockExec)
        assertSame(mockExec, mutated.executionProvider)
    }

    @Test
    fun shouldOverrideSchemaProvider(@Mock mockSchema: SchemaProvider) {
        val mutated = runner.withSchema(mockSchema)
        assertSame(mockSchema, mutated.schemaProvider)
    }

    @Test
    fun shouldOverrideSqlProvider(@Mock mockSql: SqlProvider) {
        val mutated = runner.withSql(mockSql)
        assertSame(mockSql, mutated.sqlProvider)
    }

    @Test
    fun shouldOverridePlanConverter(@Mock mockPc: PlanConverter) {
        val mutated = runner.withPlanConverter(mockPc)
        assertSame(mockPc, mutated.planConverter)
    }

    @Test
    fun shouldOverrideSecurityProvider(@Mock mockSec: SecurityProvider) {
        val mutated = runner.withSecurity(mockSec)
        assertSame(mockSec, mutated.securityProvider)
    }

    @Test
    fun shouldCascadePlanConverterToExecution(@Mock mockPc: PlanConverter) {
        val sql = PlanConverter.ConvertedPlanSql("SELECT * FROM `ts`.`TEST`", listOf())
        `when`(mockPc.toSql(any(Plan::class.java))).thenReturn(sql)

        val mutated = runner.withPlanConverter(mockPc)
        val result = mutated.executionProvider.execute(
            ImmutablePlan.builder().build(),
            QueryExecutionConfig.newBuilder().setFetchSize(10).build()
        )

        verify(mockPc).toSql(any(Plan::class.java))
        assertTrue(result.hasNext())
    }

    @Test
    fun shouldNotAffectOtherProvidersOnMutation(@Mock mockExec: ExecutionProvider) {
        val originalSchema = runner.schemaProvider
        val originalSql = runner.sqlProvider
        val originalPc = runner.planConverter

        val mutated = runner.withExecution(mockExec)

        assertSame(mockExec, mutated.executionProvider)
        assertNotSame(originalSchema, mutated.schemaProvider, "Schema provider should be a fresh instance from derive()")
        assertNotNull(mutated.schemaProvider)
        assertNotNull(mutated.sqlProvider)
        assertNotNull(mutated.planConverter)
    }

    @Test
    fun shouldRunConsumer() {
        var called = false
        runner.run { ctx ->
            called = true
            assertNotNull(ctx.executionProvider)
        }
        assertTrue(called)
    }

    @Test
    fun shouldExposeJdbcContextFactory() {
        assertNotNull(runner.jdbcContextFactory)
    }

    @Test
    fun shouldExposeCalciteContextFactory() {
        assertNotNull(runner.calciteContextFactory)
    }

    @Test
    fun shouldOverrideCalciteContextFactory(@Mock mockCf: CalciteContextFactory) {
        val mutated = runner.withCalciteContextFactory(mockCf)
        assertSame(mockCf, mutated.calciteContextFactory)
    }

}
