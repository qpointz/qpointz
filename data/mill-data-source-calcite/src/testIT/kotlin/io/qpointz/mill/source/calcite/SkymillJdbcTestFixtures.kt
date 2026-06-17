package io.qpointz.mill.source.calcite

import io.qpointz.mill.test.data.skymill.SkymillDataset
import io.qpointz.mill.test.data.skymill.SkymillExplainSupport
import io.qpointz.mill.test.data.skymill.SkymillTestFixtures
import org.apache.calcite.jdbc.CalciteConnection
import java.sql.DriverManager
import java.util.Properties

/**
 * JDBC integration fixtures for Skymill-backed [FlowTable] / [FlowTableScan] tests.
 *
 * Opens a real Calcite JDBC connection with the Skymill schema materialised via
 * [SourceSchemaManager]. Use for end-to-end `executeQuery` and production-style
 * `EXPLAIN PLAN FOR` (Volcano) assertions that unit Hep fixtures do not cover.
 */
object SkymillJdbcTestFixtures {

    private const val SELECT_BOOKINGS_COUNT_SQL =
        "SELECT COUNT(*) AS cnt FROM `skymill`.`bookings`"

    private val JOIN_WITHOUT_WHERE_SQL: String =
        """
        SELECT COUNT(*) AS cnt
        FROM `skymill`.`bookings` AS b
        INNER JOIN `skymill`.`passenger` AS p ON b.`passenger_id` = p.`id`
        INNER JOIN `skymill`.`flight_instances` AS fi ON b.`flight_instance_id` = fi.`id`
        INNER JOIN `skymill`.`segments` AS s ON fi.`segment_id` = s.`id`
        INNER JOIN `skymill`.`cities` AS c1 ON s.`origin` = c1.`id`
        INNER JOIN `skymill`.`cities` AS c2 ON s.`destination` = c2.`id`
        INNER JOIN `skymill`.`cities` AS c3 ON p.`domicile_city_id` = c3.`id`
        """.trimIndent()

    /**
     * Opens a JDBC session against the Skymill dataset.
     *
     * @param dataset CSV by default; requires `test/datasets/skymill/<format>/` under repo root
     */
    fun openSession(dataset: SkymillDataset = SkymillDataset.CSV): SkymillJdbcSession {
        val schemaManager = SourceSchemaManager()
        schemaManager.add(SkymillTestFixtures.descriptorFor(dataset))
        Class.forName("org.apache.calcite.jdbc.Driver")
        val connection = DriverManager
            .getConnection("jdbc:calcite:", calciteConnectionProperties())
            .unwrap(CalciteConnection::class.java)
        schemaManager.registerAll(connection.rootSchema)
        connection.schema = SkymillTestFixtures.SCHEMA_NAME
        return SkymillJdbcSession(connection, schemaManager)
    }

    /**
     * Baseline scalar query used by JDBC integration smoke tests.
     */
    fun selectBookingsCountSql(): String = SELECT_BOOKINGS_COUNT_SQL

    /**
     * Six-way join used by JDBC integration smoke tests (same SQL as unit-test join fixtures).
     */
    fun joinWithoutWhereSql(): String = JOIN_WITHOUT_WHERE_SQL

    private fun calciteConnectionProperties(): Properties =
        Properties().apply {
            setProperty("quoting", "BACK_TICK")
            setProperty("caseSensitive", "false")
            setProperty("unquotedCasing", "TO_UPPER")
        }
}

/**
 * Auto-closeable Skymill JDBC session for integration tests.
 *
 * @property connection Calcite JDBC connection with Skymill as default schema
 */
class SkymillJdbcSession(
    val connection: CalciteConnection,
    private val schemaManager: SourceSchemaManager,
) : AutoCloseable {

    /**
     * Runs [sql] and returns the first column of the first row as [Long].
     *
     * @param sql SQL to execute
     */
    fun queryScalarLong(sql: String): Long {
        connection.createStatement().use { statement ->
            statement.executeQuery(sql).use { rs ->
                check(rs.next()) { "query returned no rows: $sql" }
                return rs.getLong(1)
            }
        }
    }

    /**
     * Production-style physical plan via JDBC `EXPLAIN PLAN FOR`.
     *
     * @param sql query to explain
     */
    fun explainJdbcPlan(sql: String): String =
        SkymillExplainSupport.explainPlanFor(connection, sql)

    override fun close() {
        schemaManager.close()
        connection.close()
    }
}
