package io.qpointz.mill.test.data.backend

import io.qpointz.mill.data.backend.ExecutionProvider
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.backend.SqlProvider
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter
import io.qpointz.mill.security.NoneSecurityProvider
import io.qpointz.mill.security.SecurityProvider
import io.qpointz.mill.proto.QueryExecutionConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.stream.StreamSupport

@ExtendWith(MockitoExtension::class)
class CalciteBackendContextRunnerTest {

    private val runner = CalciteBackendContextRunner.calciteContext(
        modelPath = "./config/test/calcite-model.yaml"
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
        assertTrue(schemas.contains("testdb"))
    }

    @Test
    fun shouldDefaultToNoneSecurityProvider() {
        assertTrue(runner.securityProvider is NoneSecurityProvider)
    }

    @Test
    fun shouldExposeCalciteContextFactory() {
        assertNotNull(runner.calciteContextFactory)
        assertNotNull(runner.calciteContextFactory.createContext())
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
    fun shouldExecuteViaSqlAndPlanConverter() {
        val parseResult = runner.sqlProvider.parseSql("SELECT * FROM `testdb`.`CITIES`")
        assertTrue(parseResult.isSuccess)

        val result = runner.executionProvider.execute(
            parseResult.plan,
            QueryExecutionConfig.newBuilder().setFetchSize(10).build()
        )
        assertTrue(result.hasNext())
        val block = result.next()
        assertTrue(block.vectorSize > 0)
    }

    @Test
    fun shouldOverrideCalciteContextFactory(@Mock mockCf: CalciteContextFactory) {
        val mutated = runner.withCalciteContextFactory(mockCf)
        assertSame(mockCf, mutated.calciteContextFactory)
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

}
