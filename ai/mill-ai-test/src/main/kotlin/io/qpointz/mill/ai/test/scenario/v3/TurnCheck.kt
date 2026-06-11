package io.qpointz.mill.ai.test.scenario.v3

/**
 * Asserts one slice of a [TurnOutcome] against a YAML check spec.
 */
interface TurnCheck {
    /** Stable check type key matching the YAML map key (e.g. `events`). */
    val type: String

    /**
     * Runs the check.
     *
     * @param outcome Collected turn outcome.
     * @param spec Check body from YAML (value under the type key).
     * @return Result with pass/fail and optional detail message.
     */
    fun run(outcome: TurnOutcome, spec: Any?): CheckResult
}

/**
 * Outcome of a single check execution.
 *
 * @param status Pass or fail.
 * @param detail Optional failure explanation.
 */
data class CheckResult(
    val status: CheckStatus,
    val detail: String? = null,
) {
    val passed: Boolean get() = status == CheckStatus.PASS
}

/** Check pass/fail status. */
enum class CheckStatus {
    PASS,
    FAIL,
}
