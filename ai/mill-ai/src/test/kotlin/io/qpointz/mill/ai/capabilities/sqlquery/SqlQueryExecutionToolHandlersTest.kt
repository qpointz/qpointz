package io.qpointz.mill.ai.capabilities.sqlquery

import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.describeSql
import io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryToolHandlers.executeSqlBounded
import io.qpointz.mill.ai.runtime.AgentContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SqlQueryExecutionToolHandlersTest {

    private val execution = MockSqlQueryExecutionPort()

    @Test
    fun shouldDescribeSql_echoSubmittedSqlWithoutMutation() {
        val artifact = describeSql(
            execution,
            "SELECT country, COUNT(*) FROM clients GROUP BY country;",
            null,
        )
        assertThat(artifact.sql).isEqualTo("SELECT country, COUNT(*) FROM clients GROUP BY country;")
        assertThat(execution.lastDescribeRequest?.sql).isEqualTo(
            "SELECT country, COUNT(*) FROM clients GROUP BY country;",
        )
    }

    @Test
    fun shouldDescribeSql_returnSchemaWithoutRows() {
        val artifact = describeSql(execution, "SELECT country, COUNT(*) FROM clients GROUP BY country", null)

        assertThat(artifact.artifactType).isEqualTo("sql-description")
        assertThat(artifact.schema).extracting<String> { it["name"]?.toString() }
            .containsExactly("country", "client_count")
        assertThat(artifact.schema).allSatisfy { column ->
            assertThat(column).containsKeys("name", "type", "nullable")
            assertThat(column).doesNotContainKeys("idx", "precision", "scale", "length")
        }
        assertThat(artifact.source).containsEntry("kind", "execution")
        assertThat(artifact.source).containsEntry("maxRows", 1)
        assertThat(execution.lastDescribeRequest?.maxRows).isEqualTo(1)
    }

    @Test
    fun shouldExecuteSql_defaultToPagedMode() {
        val artifact = executeSqlBounded(
            execution = execution,
            sql = "SELECT country, COUNT(*) FROM clients GROUP BY country",
            resultMode = null,
            maxRows = null,
            dialect = null,
        )

        assertThat(artifact.artifactType).isEqualTo("sql-result")
        assertThat(artifact.resultMode).isEqualTo("paged")
        assertThat(artifact.rows).isNotEmpty
        assertThat(artifact.rowCount).isEqualTo(artifact.rows.size)
        assertThat(execution.lastExecuteRequest?.resultMode).isEqualTo(SqlQueryResultMode.PAGED)
    }

    @Test
    fun shouldExecuteSql_supportFullMode() {
        val artifact = executeSqlBounded(
            execution = execution,
            sql = "SELECT country, COUNT(*) FROM clients GROUP BY country",
            resultMode = "full",
            maxRows = 10,
            dialect = null,
        )

        assertThat(artifact.resultMode).isEqualTo("full")
        assertThat(execution.lastExecuteRequest?.resultMode).isEqualTo(SqlQueryResultMode.FULL)
        assertThat(artifact.limit).isEqualTo(10)
    }

    @Test
    fun shouldRejectBlankSql_forDescribeAndExecute() {
        assertThatThrownBy { describeSql(execution, "  ", null) }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy {
            executeSqlBounded(execution, "", null, null, null)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldSurfaceBackendFailure_asSqlQueryExecutionException() {
        val failing = MockSqlQueryExecutionPort(failExecute = true)
        assertThatThrownBy {
            executeSqlBounded(failing, "SELECT 1", "paged", 10, null)
        }.isInstanceOf(SqlQueryExecutionException::class.java)
            .extracting { (it as SqlQueryExecutionException).code }
            .isEqualTo("QUERY_FAILED")
    }

    @Test
    fun shouldRegisterDescribeAndExecuteTools_onSqlQueryCapability() {
        val registry = CapabilityRegistry.from(listOf(SqlQueryCapabilityProvider()))
        val capability = registry.provider("sql-query")!!.create(
            AgentContext(contextType = "general"),
            CapabilityDependencies.of(mockSqlQueryCapabilityDependency()),
        )
        val toolNames = capability.tools.map { it.spec.name() }
        assertThat(toolNames).containsExactlyInAnyOrder("validate_sql", "describe_sql", "execute_sql")
    }
}
