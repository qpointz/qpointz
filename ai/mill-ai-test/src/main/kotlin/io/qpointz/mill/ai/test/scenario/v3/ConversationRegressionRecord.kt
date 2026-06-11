package io.qpointz.mill.ai.test.scenario.v3

import java.time.Instant

/**
 * Machine-readable regression snapshot for an entire scenario pack run (`schemaVersion: 1`).
 *
 * @param schemaVersion Record format version.
 * @param recordedAt Wall-clock time the record was written.
 * @param runMeta Execution metadata (mode, profile, git commit, source path).
 * @param pack Pack identity and parameters.
 * @param summary Aggregate pass/fail counts.
 * @param turns Per-turn input, outcome, and verify results.
 */
data class ConversationRegressionRecord(
    val schemaVersion: Int = 1,
    val recordedAt: Instant,
    val runMeta: RunMeta,
    val pack: PackMeta,
    val summary: RecordSummary,
    val turns: List<TurnRecord>,
)

/**
 * Metadata about how the pack was executed.
 *
 * @param mode `scripted` or `live`.
 * @param profileId Agent profile id.
 * @param gitCommit Optional git HEAD at record time.
 * @param scenarioSource Classpath or file path of the YAML pack.
 */
data class RunMeta(
    val mode: String,
    val profileId: String,
    val gitCommit: String?,
    val scenarioSource: String,
)

/**
 * Pack identity embedded in the record.
 *
 * @param name Pack name from YAML.
 * @param parameters Pack parameters map.
 */
data class PackMeta(
    val name: String,
    val parameters: Map<String, Any?>,
)

/**
 * Aggregate statistics for a pack run.
 *
 * @param overall `PASS` or `FAIL`.
 * @param turnCount Number of turns executed.
 * @param checksPassed Passed check count.
 * @param checksFailed Failed check count.
 * @param durationMs Wall-clock duration of the pack run.
 */
data class RecordSummary(
    val overall: String,
    val turnCount: Int,
    val checksPassed: Int,
    val checksFailed: Int,
    val durationMs: Long,
)

/**
 * Single turn row in a regression record.
 *
 * @param index Zero-based turn index in the pack.
 * @param action Action type (currently `ask`).
 * @param input Turn input (ask text + script).
 * @param outcome Serialized [TurnOutcome].
 * @param verify Verification specs and per-check results.
 */
data class TurnRecord(
    val index: Int,
    val action: String,
    val input: Map<String, Any?>,
    val outcome: Map<String, Any?>,
    val verify: VerifyRecord?,
)

/**
 * Verification section stored in a regression record.
 *
 * @param passLevel YAML `pass` level.
 * @param checks Original YAML check specs.
 * @param results Per-check pass/fail results.
 */
data class VerifyRecord(
    val passLevel: String,
    val checks: List<Map<String, Any?>>,
    val results: List<Map<String, Any?>>,
)
