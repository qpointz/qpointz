package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.runtime.ConversationSession
import io.qpointz.mill.ai.test.scenario.v3.AskRunItem
import io.qpointz.mill.ai.test.scenario.v3.ScenarioPack
import io.qpointz.mill.ai.test.scenario.v3.ScenarioParameters
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class ScenarioPackRunnerTest {

    @TempDir
    lateinit var reportsDir: Path

    @Test
    fun shouldUseInjectedTurnRunner() {
        val capturedAsks = mutableListOf<String>()
        val runner = ScenarioPackRunner(
            turnRunner = AgentTurnRunner { _, item, _, _ ->
                capturedAsks += item.ask
                TurnOutcome(response = "stub-${item.ask}", events = emptyList())
            },
            recordWriter = io.qpointz.mill.ai.test.scenario.v3.ConversationRegressionWriter(reportsDir),
        )
        val pack = ScenarioPack(
            name = "injected-runner",
            profileId = "hello-world",
            parameters = ScenarioParameters(mode = "live"),
            run = listOf(AskRunItem(ask = "hello")),
        )

        val result = runner.run(pack, "test/injected.yml", mapOf("modelName" to "test-model"))

        assertThat(capturedAsks).containsExactly("hello")
        assertThat(result.passed).isTrue()
        assertThat(result.record.runMeta.modelName).isEqualTo("test-model")
        assertThat(result.record.pack.parameters["mode"]).isEqualTo("live")
    }
}
