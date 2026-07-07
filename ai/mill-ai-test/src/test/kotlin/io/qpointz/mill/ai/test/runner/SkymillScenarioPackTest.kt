package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.test.scenario.v3.ScenarioPackLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files

class SkymillScenarioPackTest {

    @Test
    fun shouldRunSkymillChatAnalysisScenario() {
        val pack = ScenarioPackLoader.fromClasspath("scenarios/skymill-chat-analysis.yml")
        val reportsDir = Files.createTempDirectory("skymill-scenario")
        val result = ScenarioPackRunner.scripted(
            recordWriter = io.qpointz.mill.ai.test.scenario.v3.ConversationRegressionWriter(reportsDir),
        ).run(pack, "scenarios/skymill-chat-analysis.yml")

        assertThat(result.failures).isEmpty()
        assertThat(result.passed).isTrue()
        assertThat(result.record.summary.turnCount).isEqualTo(10)
        assertThat(result.record.summary.checksFailed).isZero()
    }
}
