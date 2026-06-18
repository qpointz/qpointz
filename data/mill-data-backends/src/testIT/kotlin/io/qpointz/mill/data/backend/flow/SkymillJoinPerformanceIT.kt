package io.qpointz.mill.data.backend.flow

import io.qpointz.mill.proto.QueryExecutionConfig
import io.qpointz.mill.test.data.backend.FlowBackendContextRunner
import io.qpointz.mill.test.data.skymill.SkymillDataset
import io.qpointz.mill.test.data.skymill.SkymillExplainSupport
import io.qpointz.mill.test.data.skymill.SkymillTestFixtures
import io.qpointz.mill.test.data.skymill.explainPlan
import io.qpointz.mill.vectors.VectorBlockIterator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/**
 * Skymill six-join performance and plan-shape integration tests on the Flow backend.
 *
 * Join-policy correctness is covered in `mill-data-source-calcite` (WI-315). This IT focuses on
 * end-to-end timing and optional JDBC explain diagnostics after optimizations land.
 */
class SkymillJoinPerformanceIT {

    @ParameterizedTest
    @EnumSource(value = SkymillDataset::class, names = ["PARQUET", "AVRO"])
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    fun shouldCompleteFullSixJoinQueryWithinBudget(dataset: SkymillDataset) {
        assumeDatasetAvailable(dataset)
        val runner = runnerFor(dataset)
        val elapsedMs = measureMillis {
            val count = runner.executeScalarCount(JOIN_WITHOUT_WHERE_SQL)
            assertTrue(count > 0L, "expected positive join count, got $count")
        }
        log.info("six-join without WHERE on {} completed in {} ms", dataset, elapsedMs)
        assertTrue(elapsedMs < BUDGET_JOIN_WITHOUT_WHERE_MS, "exceeded budget: ${elapsedMs}ms")
    }

    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    fun shouldCompareJoinWithAndWithoutCorrelatedCitiesFilter_onParquet() {
        val runner = runnerFor(SkymillDataset.PARQUET)
        val withoutMs = measureMillis {
            val count = runner.executeScalarCount(JOIN_WITHOUT_WHERE_SQL)
            assertTrue(count > 0L)
        }
        val withMs = measureMillis {
            val count = runner.executeScalarCount(FULL_JOIN_WITH_CITIES_FILTER_SQL)
            assertTrue(count > 0L)
        }
        log.info("parquet join without WHERE: {} ms; with cities filter: {} ms (ratio {}x)",
            withoutMs, withMs, withMs.toDouble() / withoutMs.coerceAtLeast(1))
        assertTrue(withoutMs < BUDGET_JOIN_WITHOUT_WHERE_MS, "baseline join exceeded budget: ${withoutMs}ms")
        assertTrue(withMs < BUDGET_JOIN_WITH_WHERE_MS, "filtered join exceeded budget: ${withMs}ms")
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    fun shouldBreakDownExecutePhasesForFullJoin_onParquet() {
        val runner = runnerFor(SkymillDataset.PARQUET)
        val parseStart = System.nanoTime()
        val parseResult = runner.sqlProvider.parseSql(JOIN_WITHOUT_WHERE_SQL)
        val parseMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - parseStart)
        assertTrue(parseResult.isSuccess)

        val executeStart = System.nanoTime()
        drain(runner.executionProvider.execute(
            parseResult.plan,
            QueryExecutionConfig.newBuilder().setFetchSize(1_000).build(),
        ))
        val executeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - executeStart)

        log.info("parseSql={} ms execute+drain={} ms", parseMs, executeMs)
        assertTrue(parseMs < PARSE_PHASE_BUDGET_MS, "parse phase unexpectedly slow: ${parseMs}ms")
        assertTrue(executeMs < BUDGET_JOIN_WITHOUT_WHERE_MS, "execute phase exceeded budget: ${executeMs}ms")
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    fun shouldLogHashJoinBiasedPlan_whenExplainFilteredJoin_onParquet() {
        val runner = runnerFor(SkymillDataset.PARQUET)
        val explain = runner.explainPlan(FULL_JOIN_WITH_CITIES_FILTER_SQL)
        val shape = SkymillExplainSupport.parseJoinPlanShape(explain)
        log.info("filtered join explain shape: {}", shape)
        SkymillExplainSupport.assertHashJoinBiased(explain)
    }

    @Test
    @Timeout(value = 180, unit = TimeUnit.SECONDS)
    fun shouldCompareParquetVsAvroFullJoinTiming() {
        assumeTrue(SkymillTestFixtures.isSkymillAvroDatasetComplete(), "skymill avro incomplete")
        val parquetMs = measureMillis {
            runnerFor(SkymillDataset.PARQUET).executeScalarCount(JOIN_WITHOUT_WHERE_SQL)
        }
        val avroMs = measureMillis {
            runnerFor(SkymillDataset.AVRO).executeScalarCount(JOIN_WITHOUT_WHERE_SQL)
        }
        log.info("six-join without WHERE parquet={} ms avro={} ms", parquetMs, avroMs)
        assertTrue(parquetMs < BUDGET_JOIN_WITHOUT_WHERE_MS)
        assertTrue(avroMs < BUDGET_JOIN_WITHOUT_WHERE_MS)
    }

    private fun runnerFor(dataset: SkymillDataset): FlowBackendContextRunner {
        val config = when (dataset) {
            SkymillDataset.CSV -> projectRoot().resolve("data/mill-data-backends/config/test/flow-skymill.yaml")
            SkymillDataset.PARQUET -> projectRoot().resolve("data/mill-data-backends/config/test/flow-skymill-parquet.yaml")
            SkymillDataset.AVRO -> projectRoot().resolve("data/mill-data-backends/config/test/flow-skymill-avro.yaml")
        }
        return FlowBackendContextRunner.flowContext(config)
    }

    private fun projectRoot(): Path =
        Path.of(System.getProperty("flow.facet.it.root", System.getProperty("mill.repo.root", ".")))
            .toAbsolutePath()
            .normalize()

    private fun assumeDatasetAvailable(dataset: SkymillDataset) {
        if (dataset == SkymillDataset.AVRO) {
            assumeTrue(
                SkymillTestFixtures.isSkymillAvroDatasetComplete(),
                "skymill avro dataset incomplete; run make regen-skymill under test/",
            )
        }
    }

    private fun measureMillis(block: () -> Unit): Long {
        val start = System.nanoTime()
        block()
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
    }

    private fun FlowBackendContextRunner.executeScalarCount(sql: String): Long {
        val parseResult = sqlProvider.parseSql(sql)
        check(parseResult.isSuccess) { "parse failed: ${parseResult.message}" }
        val iterator = executionProvider.execute(
            parseResult.plan,
            QueryExecutionConfig.newBuilder().setFetchSize(1_000).build(),
        )
        return drainScalarLong(iterator)
    }

    private fun drain(iterator: VectorBlockIterator) {
        while (iterator.hasNext()) {
            iterator.next()
        }
    }

    private fun drainScalarLong(iterator: VectorBlockIterator): Long {
        check(iterator.hasNext()) { "query returned no vector blocks" }
        val block = iterator.next()
        check(block.vectorSize > 0) { "empty result block" }
        val vector = block.vectorsList.firstOrNull() ?: error("no vectors in result block")
        return when {
            vector.hasI64Vector() -> vector.i64Vector.getValues(0)
            vector.hasI32Vector() -> vector.i32Vector.getValues(0).toLong()
            vector.hasFp64Vector() -> vector.fp64Vector.getValues(0).toLong()
            else -> error("unsupported scalar vector type in block")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SkymillJoinPerformanceIT::class.java)

        private const val BUDGET_JOIN_WITHOUT_WHERE_MS = 90_000L
        private const val BUDGET_JOIN_WITH_WHERE_MS = 90_000L
        private const val PARSE_PHASE_BUDGET_MS = 30_000L

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

        private val FULL_JOIN_WITH_CITIES_FILTER_SQL: String =
            """
            SELECT COUNT(*) AS cnt
            FROM `skymill`.`bookings` AS b
            INNER JOIN `skymill`.`passenger` AS p ON b.`passenger_id` = p.`id`
            INNER JOIN `skymill`.`flight_instances` AS fi ON b.`flight_instance_id` = fi.`id`
            INNER JOIN `skymill`.`segments` AS s ON fi.`segment_id` = s.`id`
            INNER JOIN `skymill`.`cities` AS c1 ON s.`origin` = c1.`id`
            INNER JOIN `skymill`.`cities` AS c2 ON s.`destination` = c2.`id`
            INNER JOIN `skymill`.`cities` AS c3 ON p.`domicile_city_id` = c3.`id`
            WHERE c2.`id` = c3.`id`
            """.trimIndent()
    }
}
