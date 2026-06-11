package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.test.scenario.v3.AskRunItem
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
 * Orchestrates a full scenario pack: turn execution, checks, and regression record emission.
 *
 * Turn execution is delegated to an injected [AgentTurnRunner] — typically [ScriptedAgentRunner]
 * for deterministic packs or [ProvidedAgentRunner] when the caller supplies a live agent.
 */
class ScenarioPackRunner(
    private val turnRunner: AgentTurnRunner,
    private val checkRegistry: TurnCheckRegistry = TurnCheckRegistry.default(),
    private val recordWriter: ConversationRegressionWriter = ConversationRegressionWriter(),
) {

    /**
     * Runs all turns in a pack and writes the regression record.
     *
     * @param pack Loaded scenario pack.
     * @param scenarioSource Source path recorded in the regression file.
     * @param runMetaExtras Optional metadata merged into the record (e.g. `modelName` for live ITs).
     * @return Pack result including record paths and per-turn failures.
     */
    fun run(
        pack: ScenarioPack,
        scenarioSource: String,
        runMetaExtras: Map<String, Any?> = emptyMap(),
    ): PackRunResult {
        ScenarioActivityLogger.logPackStarted(
            packName = pack.name,
            profileId = pack.profileId,
            mode = pack.parameters.mode,
            turnCount = pack.run.size,
            source = scenarioSource,
        )
        val session = ConversationSession(profileId = pack.profileId)
        val started = System.currentTimeMillis()
        val turnRecords = mutableListOf<TurnRecord>()
        var checksPassed = 0
        var checksFailed = 0
        val failures = mutableListOf<String>()

        pack.run.forEachIndexed { index, item ->
            val outcome = turnRunner.runTurn(pack, item, index, session)
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
                input = buildTurnInput(pack.parameters.mode, item),
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
                modelName = runMetaExtras["modelName"] as? String,
            ),
            pack = PackMeta(
                name = pack.name,
                parameters = buildPackParameters(pack, runMetaExtras),
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
        ScenarioActivityLogger.logPackFinished(
            packName = pack.name,
            overall = overall,
            durationMs = record.summary.durationMs,
            failures = failures,
        )
        return PackRunResult(record, paths, failures)
    }

    companion object {
        /**
         * Runner with [ScriptedAgentRunner] for deterministic `mode: scripted` packs.
         */
        fun scripted(
            checkRegistry: TurnCheckRegistry = TurnCheckRegistry.default(),
            recordWriter: ConversationRegressionWriter = ConversationRegressionWriter(),
        ): ScenarioPackRunner =
            ScenarioPackRunner(ScriptedAgentRunner(), checkRegistry, recordWriter)
    }

    private fun buildTurnInput(mode: String, item: AskRunItem): Map<String, Any?> =
        if (mode == "live") {
            mapOf("ask" to item.ask)
        } else {
            mapOf("ask" to item.ask, "script" to item.script)
        }

    private fun buildPackParameters(
        pack: ScenarioPack,
        runMetaExtras: Map<String, Any?>,
    ): Map<String, Any?> =
        buildMap {
            put("mode", pack.parameters.mode)
            runMetaExtras.forEach { (key, value) ->
                if (value != null) put(key, value)
            }
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
