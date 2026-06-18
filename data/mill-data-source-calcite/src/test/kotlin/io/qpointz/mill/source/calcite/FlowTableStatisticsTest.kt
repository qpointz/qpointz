package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.SourceResolver
import io.qpointz.mill.source.SourceTable
import io.qpointz.mill.source.statistics.RecordStatistic
import io.qpointz.mill.source.statistics.RecordStatisticProvider
import io.qpointz.mill.source.statistics.SourceTableStatisticProviders
import io.qpointz.mill.test.data.calcite.RelOptTestSupport
import io.qpointz.mill.test.data.skymill.SkymillDataset
import io.qpointz.mill.test.data.skymill.SkymillTestFixtures
import io.qpointz.mill.types.sql.DatabaseType
import io.qpointz.mill.vectors.VectorBlockIterator
import org.apache.calcite.schema.Statistics
import org.apache.calcite.tools.Frameworks
import org.apache.calcite.util.ImmutableBitSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FlowTableStatisticsTest {

    @Test
    fun shouldReturnUnknownStatistic_whenRecordProviderNotWired() {
        val table = FlowTable(stubSourceTable())
        assertEquals(Statistics.UNKNOWN, table.statistic)
    }

    @Test
    fun shouldExposeIdKey_whenKeyProviderWired() {
        val table = FlowTable(
            stubSourceTable(
                providers = SourceTableStatisticProviders.of(
                    record = RecordStatisticProvider { RecordStatistic(estimatedRowCount = 42L) },
                    key = io.qpointz.mill.source.statistics.SchemaKeyStatisticProvider(
                        RecordSchema.of(
                            "id" to DatabaseType.i32(false),
                            "name" to DatabaseType.string(true, 100),
                        ),
                    ),
                ),
            ),
        )

        val statistic = table.statistic
        val keys = requireNotNull(statistic.keys) { "expected unique keys" }
        assertEquals(1, keys.size)
        assertEquals(ImmutableBitSet.of(0), keys.single())
        assertEquals(42.0, statistic.rowCount!!, 0.0)
    }

    @Test
    fun shouldExposeRowCountViaMetadataQuery_whenSkymillParquetPlanned() {
        SkymillCalciteTestFixtures.openSession(SkymillDataset.PARQUET).use { session ->
            val frameworkConfig = SkymillCalciteTestFixtures.frameworkConfig(session)
            val planner = Frameworks.getPlanner(frameworkConfig)
            val rel = planner.rel(
                planner.validate(
                    planner.parse("SELECT `id` FROM `skymill`.`cities`")
                ),
            ).rel

            val scan = RelOptTestSupport.findNodes(rel, FlowTableScan::class.java).single()
            val rowCount = scan.cluster.metadataQuery.getRowCount(scan)
            assertEquals(14.0, rowCount, 0.0)
        }
    }

    @Test
    fun shouldMatchSkymillParquetRowCounts_whenMaterialized() {
        val resolved = SourceResolver.resolveDescriptor(SkymillTestFixtures.descriptorFor(SkymillDataset.PARQUET))
        resolved.use { source ->
            assertTableRowCount(source, "cities", 14L)
            assertTableRowCount(source, "passenger", 210L)
            assertTableRowCount(source, "bookings", 1_050L)
        }
    }

    @Test
    fun shouldMatchActualRecordCounts_whenSkymillParquetMaterialized() {
        val resolved = SourceResolver.resolveDescriptor(SkymillTestFixtures.descriptorFor(SkymillDataset.PARQUET))
        resolved.use { source ->
            for (tableName in listOf("cities", "passenger", "bookings")) {
                val sourceTable = source.tables.getValue(tableName)
                assertTrue(sourceTable.statisticProviders().recordStatistic().isPresent)
                val estimated = sourceTable.statisticProviders().recordStatistic().get()
                    .recordStatistic()?.estimatedRowCount
                assertTrue(estimated != null, "expected estimated row count for $tableName")
                val actual = sourceTable.records().count().toLong()
                assertEquals(actual, estimated, "estimated row count for $tableName")
            }
        }
    }

    @Test
    fun shouldMatchSkymillCsvRowCounts_whenMaterialized() {
        val resolved = SourceResolver.resolveDescriptor(SkymillTestFixtures.descriptorFor(SkymillDataset.CSV))
        resolved.use { source ->
            assertTableRowCount(source, "cities", 14L)
        }
    }

    @Test
    fun shouldWireCsvRecordStatistics_whenSkymillMaterialized() {
        val resolved = SourceResolver.resolveDescriptor(SkymillTestFixtures.descriptorFor(SkymillDataset.CSV))
        resolved.use { source ->
            for (tableName in listOf("cities", "passenger", "bookings")) {
                val sourceTable = source.tables.getValue(tableName)
                assertTrue(sourceTable.statisticProviders().recordStatistic().isPresent)
                val estimated = sourceTable.statisticProviders().recordStatistic().get()
                    .recordStatistic()?.estimatedRowCount
                assertTrue(estimated != null && estimated > 0, "expected positive estimate for $tableName")
                val actual = sourceTable.records().count().toLong()
                assertTrue(
                    estimated!! >= actual,
                    "line heuristic should not under-count $tableName (estimated=$estimated actual=$actual)",
                )
            }
        }
    }

    private fun assertTableRowCount(source: io.qpointz.mill.source.ResolvedSource, tableName: String, expected: Long) {
        val flowTable = FlowTable(source.tables.getValue(tableName))
        val rowCount = flowTable.statistic.rowCount
        assertTrue(rowCount != null, "expected row count statistic for $tableName")
        assertEquals(expected.toDouble(), rowCount!!, 0.0, "row count for $tableName")
    }

    private fun stubSourceTable(
        schema: RecordSchema = RecordSchema.of("id" to DatabaseType.i32(false)),
        providers: SourceTableStatisticProviders = SourceTableStatisticProviders.none(),
    ): SourceTable = object : SourceTable {
        override val schema: RecordSchema = schema
        override fun statisticProviders(): SourceTableStatisticProviders = providers
        override fun records(): Iterable<Record> = emptyList()
        override fun vectorBlocks(batchSize: Int): VectorBlockIterator {
            throw UnsupportedOperationException("not used")
        }
    }
}
