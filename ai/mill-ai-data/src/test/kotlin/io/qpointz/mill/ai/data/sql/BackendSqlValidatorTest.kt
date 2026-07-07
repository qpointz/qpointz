package io.qpointz.mill.ai.data.sql

import io.qpointz.mill.data.backend.SqlProvider
import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import io.qpointz.mill.sql.v2.dialect.SqlStatementNormalizer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class BackendSqlValidatorTest {

    @Test
    fun `strips trailing semicolon for calcite dialect before parseSql`() {
        val sqlProvider = mock<SqlProvider>()
        whenever(sqlProvider.parseSql(any())).thenReturn(SqlProvider.PlanParseResult.success(null))
        val calcite = DialectRegistry.fromClasspathDefaults().requireDialect("CALCITE")
        val v = BackendSqlValidator(sqlProvider, calcite)
        val outcome = v.validate("SELECT 1;")
        assertThat(outcome.passed).isTrue()
        assertThat(outcome.normalizedSql).isEqualTo("SELECT 1")
    }

    @Test
    fun `strips trailing semicolon without dialect spec before parseSql`() {
        val sqlProvider = mock<SqlProvider>()
        whenever(sqlProvider.parseSql(any())).thenReturn(SqlProvider.PlanParseResult.success(null))
        val v = BackendSqlValidator(sqlProvider)
        val outcome = v.validate("SELECT 1;")
        assertThat(outcome.passed).isTrue()
        assertThat(outcome.normalizedSql).isEqualTo("SELECT 1")
    }

    @Test
    fun `passes when parseSql succeeds`() {
        val sqlProvider = mock<SqlProvider>()
        whenever(sqlProvider.parseSql(any())).thenReturn(SqlProvider.PlanParseResult.success(null))
        val v = BackendSqlValidator(sqlProvider)
        val outcome = v.validate("SELECT 1")
        assertThat(outcome.passed).isTrue()
        assertThat(outcome.normalizedSql).isEqualTo("SELECT 1")
    }

    @Test
    fun `fails when parseSql fails`() {
        val sqlProvider = mock<SqlProvider>()
        whenever(sqlProvider.parseSql(any())).thenReturn(SqlProvider.PlanParseResult.fail("bad sql"))
        val v = BackendSqlValidator(sqlProvider)
        val outcome = v.validate("SELECT FROM")
        assertThat(outcome.passed).isFalse()
        assertThat(outcome.message).contains("bad sql")
    }
}
