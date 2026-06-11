package io.qpointz.mill.ai.test

/**
 * Live LLM scenario packs (WI-310). Opt-in: requires `OPENAI_API_KEY`.
 *
 * ```bash
 * OPENAI_API_KEY=sk-... ./gradlew :ai:mill-ai-test:testIT --tests "LiveScenarioPacksIT"
 * ```
 */
class LiveScenarioPacksIT : LiveScenarioPackTestBase() {

    override fun scenarioResources(): List<String> = listOf(
        "scenarios/live/live-hello-smoke.yml",
    )
}
