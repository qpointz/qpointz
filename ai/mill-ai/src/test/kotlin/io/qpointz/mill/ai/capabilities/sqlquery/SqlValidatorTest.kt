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

    @Test
    fun shouldUseInputSql_whenValidatorPassesWithoutNormalizedSql() {
        val validator = SqlQueryToolHandlers.SqlValidationService {
            SqlQueryToolHandlers.ValidationResult(passed = true, message = null, normalizedSql = null)
        }
        val artifact = SqlQueryToolHandlers.validateSql(
            validator,
            "  SELECT month FROM sales  ",
            1,
            title = "Monthly sales",
            description = "Lists distinct months present in the sales table.",
        )
        assertTrue(artifact.passed)
        assertEquals("SELECT month FROM sales", artifact.normalizedSql)
    }

    @Test
    fun shouldStripTrailingSemicolonInNormalizedSql_whenValidatorOmitsIt() {
        val validator = SqlQueryToolHandlers.SqlValidationService {
            SqlQueryToolHandlers.ValidationResult(passed = true, message = null, normalizedSql = null)
        }
        val artifact = SqlQueryToolHandlers.validateSql(
            validator,
            "SELECT month FROM sales;",
            1,
            title = "Monthly sales",
            description = "Lists distinct months present in the sales table.",
        )
        assertTrue(artifact.passed)
        assertEquals("SELECT month FROM sales", artifact.normalizedSql)
    }
}
