package io.qpointz.mill.source.calcite

import io.qpointz.mill.test.data.skymill.SkymillDataset
import io.qpointz.mill.test.data.skymill.SkymillExplainSupport
import io.qpointz.mill.test.data.skymill.SkymillTestFixtures
import org.apache.calcite.tools.Frameworks
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.concurrent.TimeUnit

/**
 * Smoke tests for Skymill join fixtures.
 */
class SkymillJoinFixturesTest {

    @ParameterizedTest
    @EnumSource(SkymillDataset::class)
    fun shouldParseSixJoinSql(dataset: SkymillDataset) {
        assumeAvroDatasetIfNeeded(dataset)
        SkymillCalciteTestFixtures.openSession(dataset).use { session ->
            val frameworkConfig = SkymillCalciteTestFixtures.frameworkConfig(session)
            for (sql in listOf(SkymillJoinQueries.JOIN_WITHOUT_WHERE, SkymillJoinQueries.FULL_JOIN_WITH_CITIES_FILTER)) {
                val planner = Frameworks.getPlanner(frameworkConfig)
                val parsed = planner.parse(sql)
                val validated = planner.validate(parsed)
                assertTrue(validated != null, "validation failed for $sql on $dataset")
            }
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    fun shouldProducePhysicalExplainForFullJoin_onCsv() {
        SkymillCalciteTestFixtures.openSession(SkymillDataset.CSV).use { session ->
            val explain = session.explainPhysicalPlan(SkymillJoinQueries.FULL_JOIN_WITH_CITIES_FILTER)
            assertTrue(explain.isNotBlank())
            val shape = SkymillExplainSupport.parseJoinPlanShape(explain)
            assertTrue(shape.hashJoinCount + shape.mergeJoinCount > 0, "expected physical join in:\n$explain")
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    //@Disabled("Enable after WI-315: FlowEnumerableRuleSets hash join bias")
    fun shouldPreferHashJoin_whenCorrelatedCitiesFilter() {
        SkymillCalciteTestFixtures.openSession(SkymillDataset.CSV).use { session ->
            val explain = session.explainPhysicalPlan(SkymillJoinQueries.FULL_JOIN_WITH_CITIES_FILTER)
            SkymillExplainSupport.assertHashJoinBiased(explain)
        }
    }

    private fun assumeAvroDatasetIfNeeded(dataset: SkymillDataset) {
        if (dataset != SkymillDataset.AVRO) {
            return
        }
        assumeTrue(
            SkymillTestFixtures.isSkymillAvroDatasetComplete(),
            "skymill avro dataset incomplete (missing flight_instances.avro); run make regen-skymill under test/",
        )
    }
}
