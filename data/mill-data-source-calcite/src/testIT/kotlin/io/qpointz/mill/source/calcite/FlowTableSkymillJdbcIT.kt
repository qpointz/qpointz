package io.qpointz.mill.source.calcite

import io.qpointz.mill.test.data.skymill.SkymillDataset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

/**
 * JDBC integration tests for [FlowTableScan] against the Skymill dataset.
 */
class FlowTableSkymillJdbcIT {

    @Test
    fun shouldReturnBookingCount_whenJdbcQueryOnSkymillCsv() {
        SkymillJdbcTestFixtures.openSession(SkymillDataset.CSV).use { session ->
            val count = session.queryScalarLong(SkymillJdbcTestFixtures.selectBookingsCountSql())
            assertEquals(1_050L, count)
        }
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    fun shouldReturnJoinCount_whenJdbcQueryOnSkymillCsv() {
        SkymillJdbcTestFixtures.openSession(SkymillDataset.CSV).use { session ->
            val count = session.queryScalarLong(SkymillJdbcTestFixtures.joinWithoutWhereSql())
            assertTrue(count > 0L, "expected positive join count, got $count")
        }
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    fun shouldShowEnumerableTableScan_whenExplainJdbcPlanForSimpleSelect() {
        SkymillJdbcTestFixtures.openSession(SkymillDataset.CSV).use { session ->
            val explain = session.explainJdbcPlan(SkymillJdbcTestFixtures.selectBookingsCountSql())
            assertTrue(
                explain.contains("EnumerableTableScan"),
                "expected EnumerableTableScan in JDBC explain:\n$explain",
            )
        }
    }
}
