package io.qpointz.mill.ai.test.scenario.v3

import io.qpointz.mill.ai.runtime.events.AgentEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.time.Instant

class ConversationRegressionRecordTest {

    @Test
    fun shouldRoundTripOutcomeAndReplayChecks() {
        val outcome = TurnOutcome(
            response = "Hello",
            events = listOf(
                AgentEvent.RunStarted("hello-world"),
                AgentEvent.AnswerCompleted("Hello"),
            ),
        )
        val map = TurnOutcomeSerializer.toMap(outcome)
        val restored = TurnOutcomeSerializer.fromMap(map)
        assertThat(restored.response).isEqualTo("Hello")
        assertThat(restored.events.map { it.type }).containsExactly("run.started", "answer.completed")

        val turnRecord = TurnRecord(
            index = 0,
            action = "ask",
            input = emptyMap(),
            outcome = map,
            verify = VerifyRecord(
                passLevel = "ERROR",
                checks = listOf(mapOf("response" to mapOf("assert" to "not-blank"))),
                results = emptyList(),
            ),
        )
        val replayed = ConversationRegressionComparator().replayTurnChecks(turnRecord)
        assertThat(replayed.single().result.passed).isTrue()
    }

    @Test
    fun shouldWriteRecordFiles() {
        val pack = ScenarioPack(name = "writer-test", profileId = "hello-world")
        val record = ConversationRegressionRecord(
            recordedAt = Instant.parse("2026-06-11T12:00:00Z"),
            runMeta = RunMeta("scripted", "hello-world", "abc", "test.yml"),
            pack = PackMeta("writer-test", mapOf("mode" to "scripted")),
            summary = RecordSummary("PASS", 1, 1, 0, 10),
            turns = emptyList(),
        )
        val paths = ConversationRegressionWriter().write(pack, record)
        assertThat(Files.exists(paths.raw)).isTrue()
        assertThat(Files.exists(paths.normalized)).isTrue()
    }
}
