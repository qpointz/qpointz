package io.qpointz.mill.ai.data.sql

import io.qpointz.mill.sql.v2.dialect.DialectRegistry
import io.qpointz.mill.sql.v2.dialect.SqlStatementNormalizer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SqlStatementNormalizerTest {

    @Test
    fun shouldStripTrailingSemicolonForCalcite() {
        val calcite = DialectRegistry.fromClasspathDefaults().requireDialect("CALCITE")
        assertThat(SqlStatementNormalizer.normalize("SELECT 1;", calcite)).isEqualTo("SELECT 1")
    }

    @Test
    fun shouldStripTrailingSemicolonWhenDialectSpecIsNull() {
        assertThat(SqlStatementNormalizer.normalize("SELECT 1;", null)).isEqualTo("SELECT 1")
    }

    @Test
    fun shouldTrimWithoutDialectSpec() {
        assertThat(SqlStatementNormalizer.normalize("  SELECT 1  ", null)).isEqualTo("SELECT 1")
    }
}
