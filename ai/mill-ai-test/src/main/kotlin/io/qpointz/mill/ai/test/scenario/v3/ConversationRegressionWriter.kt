package io.qpointz.mill.ai.test.scenario.v3

import tools.jackson.databind.json.JsonMapper
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

/**
 * Persists [ConversationRegressionRecord] JSON files under `build/reports/scenarios/`.
 */
class ConversationRegressionWriter(
    private val reportsDir: Path = Path.of("build", "reports", "scenarios"),
    private val jsonMapper: JsonMapper = ScenarioPackLoader.jsonMapper,
) {

    /**
     * Writes raw and normalized record files for a pack run.
     *
     * @param pack Scenario pack (used for slug).
     * @param record Regression record to persist.
     * @return Paths to the raw and normalized files.
     */
    fun write(pack: ScenarioPack, record: ConversationRegressionRecord): WrittenRecordPaths {
        Files.createDirectories(reportsDir)
        val slug = pack.slug()
        val recordMap = recordToMap(record)
        val rawPath = reportsDir.resolve("$slug.record.json")
        val normalizedPath = reportsDir.resolve("$slug.record.normalized.json")
        val rawJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(recordMap)
        Files.writeString(rawPath, rawJson)
        val normalizedJson = RecordNormalizer.normalizeJson(rawJson)
        Files.writeString(normalizedPath, normalizedJson)
        return WrittenRecordPaths(rawPath, normalizedPath)
    }

    private fun recordToMap(record: ConversationRegressionRecord): Map<String, Any?> = mapOf(
        "schemaVersion" to record.schemaVersion,
        "recordedAt" to record.recordedAt.toString(),
        "runMeta" to buildMap {
            put("mode", record.runMeta.mode)
            put("profileId", record.runMeta.profileId)
            put("gitCommit", record.runMeta.gitCommit)
            put("scenarioSource", record.runMeta.scenarioSource)
            record.runMeta.modelName?.let { put("modelName", it) }
        },
        "pack" to mapOf(
            "name" to record.pack.name,
            "parameters" to record.pack.parameters,
        ),
        "summary" to mapOf(
            "overall" to record.summary.overall,
            "turnCount" to record.summary.turnCount,
            "checksPassed" to record.summary.checksPassed,
            "checksFailed" to record.summary.checksFailed,
            "durationMs" to record.summary.durationMs,
        ),
        "turns" to record.turns.map { turn ->
            mapOf(
                "index" to turn.index,
                "action" to turn.action,
                "input" to turn.input,
                "outcome" to turn.outcome,
                "verify" to turn.verify?.let { v ->
                    mapOf(
                        "passLevel" to v.passLevel,
                        "checks" to v.checks,
                        "results" to v.results,
                    )
                },
            )
        },
    )
}

/**
 * Paths written by [ConversationRegressionWriter.write].
 *
 * @param raw Full record JSON path.
 * @param normalized Scrubbed record JSON path.
 */
data class WrittenRecordPaths(
    val raw: Path,
    val normalized: Path,
)
