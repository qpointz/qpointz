package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.test.scenario.v3.ConversationRegressionRecord
import io.qpointz.mill.ai.test.scenario.v3.ConversationRegressionWriter
import io.qpointz.mill.ai.test.scenario.v3.PackMeta
import io.qpointz.mill.ai.test.scenario.v3.PassLevel
import io.qpointz.mill.ai.test.scenario.v3.RecordSummary
import io.qpointz.mill.ai.test.scenario.v3.RunMeta
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPack
import io.qpointz.mill.ai.test.scenario.v3.TurnCheckRegistry
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcomeSerializer
import io.qpointz.mill.ai.test.scenario.v3.TurnRecord
import io.qpointz.mill.ai.test.scenario.v3.VerifyRecord
import io.qpointz.mill.ai.test.scenario.v3.WrittenRecordPaths
import java.time.Instant

/**
 * Orchestrates a full scenario pack: scripted turns, checks, and regression record emission.
 */
class ScenarioPackRunner(
    private val agentRunner: ScriptedAgentRunner = ScriptedAgentRunner(),
    private val checkRegistry: TurnCheckRegistry = TurnCheckRegistry.default(),
    private val recordWriter: ConversationRegressionWriter = ConversationRegressionWriter(),
) {

    /**
     * Runs all turns in a pack and writes the regression record.
     *
     * @param pack Loaded scenario pack.
     * @param scenarioSource Source path recorded in the regression file.
     * @return Pack result including record paths and per-turn failures.
     */
    fun run(pack: ScenarioPack, scenarioSource: String): PackRunResult {
        val started = System.currentTimeMillis()
        val turnRecords = mutableListOf<TurnRecord>()
        var checksPassed = 0
        var checksFailed = 0
        val failures = mutableListOf<String>()

        pack.run.forEachIndexed { index, item ->
            val outcome = agentRunner.runTurn(pack, item, index)
            val verify = item.verify
            val namedResults = if (verify != null) checkRegistry.runAll(outcome, verify) else emptyList()
            namedResults.forEach { named ->
                if (named.result.passed) checksPassed++ else checksFailed++
                if (!named.result.passed && verify?.pass == PassLevel.ERROR) {
                    failures += "turn $index check ${named.checkType}: ${named.result.detail}"
                }
            }
            turnRecords += TurnRecord(
                index = index,
                action = "ask",
                input = mapOf(
                    "ask" to item.ask,
                    "script" to item.script,
                ),
                outcome = TurnOutcomeSerializer.toMap(outcome),
                verify = verify?.let { v ->
                    VerifyRecord(
                        passLevel = v.pass.name,
                        checks = v.check,
                        results = namedResults.map {
                            mapOf(
                                "checkType" to it.checkType,
                                "status" to it.result.status.name,
                                "detail" to it.result.detail,
                            )
                        },
                    )
                },
            )
        }

        val overall = if (failures.isEmpty()) "PASS" else "FAIL"
        val record = ConversationRegressionRecord(
            recordedAt = Instant.now(),
            runMeta = RunMeta(
                mode = pack.parameters.mode,
                profileId = pack.profileId,
                gitCommit = resolveGitCommit(),
                scenarioSource = scenarioSource,
            ),
            pack = PackMeta(
                name = pack.name,
                parameters = mapOf("mode" to pack.parameters.mode),
            ),
            summary = RecordSummary(
                overall = overall,
                turnCount = pack.run.size,
                checksPassed = checksPassed,
                checksFailed = checksFailed,
                durationMs = System.currentTimeMillis() - started,
            ),
            turns = turnRecords,
        )
        val paths = recordWriter.write(pack, record)
        return PackRunResult(record, paths, failures)
    }

    private fun resolveGitCommit(): String? =
        runCatching {
            ProcessBuilder("git", "rev-parse", "HEAD")
                .directory(java.io.File("."))
                .start()
                .inputStream.bufferedReader()
                .readText()
                .trim()
                .ifBlank { null }
        }.getOrNull()
}

/**
 * Result of running a full scenario pack.
 *
 * @param record Regression record that was written.
 * @param paths Output file paths.
 * @param failures Human-readable failure messages (empty when pass).
 */
data class PackRunResult(
    val record: ConversationRegressionRecord,
    val paths: WrittenRecordPaths,
    val failures: List<String>,
) {
    val passed: Boolean get() = failures.isEmpty() && record.summary.overall == "PASS"
}
