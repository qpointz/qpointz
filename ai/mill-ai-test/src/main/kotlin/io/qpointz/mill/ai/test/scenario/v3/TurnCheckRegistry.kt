package io.qpointz.mill.ai.test.scenario.v3

import io.qpointz.mill.ai.test.scenario.v3.checks.ArtifactsTurnCheck
import io.qpointz.mill.ai.test.scenario.v3.checks.EventsTurnCheck
import io.qpointz.mill.ai.test.scenario.v3.checks.ResponseTurnCheck
import io.qpointz.mill.ai.test.scenario.v3.checks.SseTurnCheck
import io.qpointz.mill.ai.test.scenario.v3.checks.TranscriptTurnCheck

/**
 * Registry of polymorphic turn checks keyed by YAML map key (`events`, `artifacts`, …).
 */
class TurnCheckRegistry(
    private val checks: Map<String, TurnCheck> = defaultChecks(),
) {

    /**
     * Runs all checks from a [VerifySpec] against an outcome.
     *
     * @param outcome Turn result to assert on.
     * @param verify Verification block from the scenario YAML.
     * @return Per-check results in YAML order.
     */
    fun runAll(outcome: TurnOutcome, verify: VerifySpec): List<NamedCheckResult> =
        verify.check.map { specMap ->
            val entry = specMap.entries.singleOrNull()
                ?: return@map NamedCheckResult("unknown", CheckResult(CheckStatus.FAIL, "check map must have exactly one key"))
            val check = checks[entry.key]
                ?: return@map NamedCheckResult(entry.key, CheckResult(CheckStatus.FAIL, "unknown check type: ${entry.key}"))
            NamedCheckResult(entry.key, check.run(outcome, entry.value))
        }

    /**
     * Re-runs checks from a saved record without executing the agent (offline replay).
     *
     * @param outcome Outcome slice from a regression record.
     * @param checkSpecs Original YAML check specs.
     * @return Per-check results.
     */
    fun replay(outcome: TurnOutcome, checkSpecs: List<Map<String, Any?>>): List<NamedCheckResult> =
        runAll(outcome, VerifySpec(check = checkSpecs))

    /**
     * Registers an additional check type.
     *
     * @param check Check implementation; [TurnCheck.type] must be unique.
     * @return New registry including the check.
     */
    fun register(check: TurnCheck): TurnCheckRegistry {
        require(check.type !in checks) { "check type already registered: ${check.type}" }
        return TurnCheckRegistry(checks + (check.type to check))
    }

    companion object {
        /** Default built-in checks for v3 scenario packs. */
        fun default(): TurnCheckRegistry = TurnCheckRegistry()

        private fun defaultChecks(): Map<String, TurnCheck> = listOf(
            EventsTurnCheck(),
            ArtifactsTurnCheck(),
            SseTurnCheck(),
            ResponseTurnCheck(),
            TranscriptTurnCheck(),
        ).associateBy { it.type }
    }
}

/**
 * Named result pairing a check type with its outcome.
 *
 * @param checkType YAML check key.
 * @param result Pass/fail result.
 */
data class NamedCheckResult(
    val checkType: String,
    val result: CheckResult,
)
