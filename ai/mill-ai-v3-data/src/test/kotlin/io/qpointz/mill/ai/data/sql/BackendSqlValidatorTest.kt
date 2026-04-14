package io.qpointz.mill.ai.data.sql

import io.qpointz.mill.data.backend.SqlProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class BackendSqlValidatorTest {

    @Test
    fun `passes when parseSql succeeds`() {
        val sqlProvider = mock<SqlProvider>()
        whenever(sqlProvider.parseSql(any())).thenReturn(SqlProvider.PlanParseResult.success(null))
        val v = BackendSqlValidator(sqlProvider)
        assertThat(v.validate("SELECT 1").passed).isTrue()
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
