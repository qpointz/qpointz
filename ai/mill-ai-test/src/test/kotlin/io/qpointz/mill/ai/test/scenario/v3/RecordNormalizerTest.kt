package io.qpointz.mill.ai.test.scenario.v3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class RecordNormalizerTest {

    @Test
    fun shouldScrubVolatileFieldsDeterministically() {
        val id = UUID.randomUUID().toString()
        val raw = mapOf(
            "schemaVersion" to 1,
            "recordedAt" to "2026-06-11T12:00:00Z",
            "runMeta" to mapOf(
                "mode" to "scripted",
                "profileId" to "hello-world",
                "gitCommit" to "deadbeef",
                "scenarioSource" to "test.yml",
            ),
            "turns" to listOf(
                mapOf(
                    "outcome" to mapOf(
                        "events" to listOf(
                            mapOf("type" to "run.started", "eventId" to id),
                        ),
                    ),
                ),
            ),
        )
        val once = RecordNormalizer.normalize(raw)
        val twice = RecordNormalizer.normalize(raw)
        assertThat(once).isEqualTo(twice)
        assertThat(once).doesNotContainKey("recordedAt")
        @Suppress("UNCHECKED_CAST")
        val runMeta = once["runMeta"] as Map<String, Any?>
        assertThat(runMeta).doesNotContainKey("gitCommit")
    }
}
