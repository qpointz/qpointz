package io.qpointz.mill.ai.test.scenario.v3

import java.nio.file.Files
import java.nio.file.Path

/**
 * Compares regression records against committed baselines and supports offline check replay.
 */
class ConversationRegressionComparator(
    private val checkRegistry: TurnCheckRegistry = TurnCheckRegistry.default(),
) {

    /**
     * Asserts normalized actual JSON matches the baseline file content.
     *
     * @param actualNormalized Normalized JSON string from the current run.
     * @param baselinePath Committed baseline path.
     */
    fun assertMatchesBaseline(actualNormalized: String, baselinePath: Path) {
        require(Files.exists(baselinePath)) { "baseline not found: $baselinePath" }
        val expected = Files.readString(baselinePath)
        if (actualNormalized.trim() != expected.trim()) {
            error("regression baseline mismatch for $baselinePath")
        }
    }

    /**
     * Re-runs checks from a saved turn record against its stored outcome.
     *
     * @param turnRecord Turn section from a regression record.
     * @return Check results; fails if any check fails.
     */
    @Suppress("UNCHECKED_CAST")
    fun replayTurnChecks(turnRecord: TurnRecord): List<NamedCheckResult> {
        val verify = turnRecord.verify ?: return emptyList()
        val outcome = TurnOutcomeSerializer.fromMap(turnRecord.outcome)
        val results = checkRegistry.replay(outcome, verify.checks)
        val failed = results.filter { !it.result.passed }
        if (failed.isNotEmpty()) {
            error("offline replay failed: ${failed.map { it.checkType to it.result.detail }}")
        }
        return results
    }

    /**
     * Writes or compares baseline depending on `UPDATE_BASELINES` env var.
     *
     * @param normalizedJson Normalized record JSON.
     * @param baselinePath Target baseline path under test resources.
     */
    fun updateOrAssert(normalizedJson: String, baselinePath: Path) {
        if (System.getenv("UPDATE_BASELINES") == "1") {
            Files.createDirectories(baselinePath.parent)
            Files.writeString(baselinePath, normalizedJson)
        } else if (Files.exists(baselinePath)) {
            assertMatchesBaseline(normalizedJson, baselinePath)
        }
    }
}
