package io.qpointz.mill.test.data.backend

import io.qpointz.mill.data.backend.ExecutionProvider
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.backend.SqlProvider
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter
import io.qpointz.mill.security.NoneSecurityProvider
import io.qpointz.mill.security.SecurityProvider
import io.qpointz.mill.data.backend.flow.FlowContextFactory
import io.qpointz.mill.data.backend.flow.SingleFileSourceRepository
import io.qpointz.mill.proto.QueryExecutionConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.file.Path
import java.util.stream.StreamSupport

@ExtendWith(MockitoExtension::class)
class FlowBackendContextRunnerTest {

    private val runner = FlowBackendContextRunner.flowContext(
        Path.of("./config/test/flow-skymill.yaml")
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
        assertTrue(schemas.contains("skymill"))
    }

    @Test
    fun shouldDefaultToNoneSecurityProvider() {
        assertTrue(runner.securityProvider is NoneSecurityProvider)
    }

    @Test
    fun shouldExposeCalciteContextFactory() {
        assertNotNull(runner.calciteContextFactory)
        assertTrue(runner.calciteContextFactory is FlowContextFactory)
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
    fun shouldOverrideCalciteContextFactory(@Mock mockCf: CalciteContextFactory) {
        val mutated = runner.withCalciteContextFactory(mockCf)
        assertSame(mockCf, mutated.calciteContextFactory)
    }

    @Test
    fun shouldExecuteViaSqlAndPlanConverter() {
        val parseResult = runner.sqlProvider.parseSql("SELECT * FROM `skymill`.`cities`")
        assertTrue(parseResult.isSuccess)

        val result = runner.executionProvider.execute(
            parseResult.plan,
            QueryExecutionConfig.newBuilder().setFetchSize(100).build()
        )
        assertTrue(result.hasNext())
        val block = result.next()
        assertTrue(block.vectorSize > 0)
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
    fun shouldConstructFromRepository() {
        val repo = SingleFileSourceRepository(Path.of("./config/test/flow-source.yaml"))
        val fromRepo = FlowBackendContextRunner.flowContext(repo)
        assertNotNull(fromRepo.schemaProvider)
        val schemas = StreamSupport.stream(fromRepo.schemaProvider.schemaNames.spliterator(), false)
            .toList()
        assertTrue(schemas.contains("flowtest"))
    }

    @Test
    fun shouldConstructFromPathList() {
        val fromList = FlowBackendContextRunner.flowContext(
            listOf(Path.of("./config/test/flow-source.yaml"))
        )
        assertNotNull(fromList.schemaProvider)
    }
}
