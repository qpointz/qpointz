package io.qpointz.mill.ai.capabilities.sqlquery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SqlValidatorTest {

    @Test
    fun shouldRoundTrip_throughAsSqlValidationService() {
        val validator = SqlValidator { sql ->
            SqlValidationOutcome(passed = sql.contains("ok"), message = null, normalizedSql = sql.trim())
        }
        val svc = validator.asSqlValidationService()
        val ok = svc.validate(" ok ")
        assertTrue(ok.passed)
        assertEquals("ok", ok.normalizedSql)
        val bad = svc.validate("nope")
        assertFalse(bad.passed)
    }

    @Test
    fun shouldMapValidationResult_toOutcome() {
        val vr = SqlQueryToolHandlers.ValidationResult(true, null, "SELECT 1")
        assertEquals("SELECT 1", vr.toSqlValidationOutcome().normalizedSql)
    }
}
