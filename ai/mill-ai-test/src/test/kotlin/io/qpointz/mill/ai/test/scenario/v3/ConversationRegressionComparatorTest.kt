package io.qpointz.mill.ai.test.scenario.v3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ConversationRegressionComparatorTest {

    @Test
    fun shouldMatchIdenticalNormalizedBaseline(@TempDir dir: Path) {
        val json = """{"schemaVersion":1,"pack":{"name":"x"}}"""
        val baseline = dir.resolve("baseline.json")
        Files.writeString(baseline, json)
        ConversationRegressionComparator().assertMatchesBaseline(json, baseline)
    }

    @Test
    fun shouldDetectBaselineDrift(@TempDir dir: Path) {
        val baseline = dir.resolve("baseline.json")
        Files.writeString(baseline, """{"schemaVersion":1}""")
        try {
            ConversationRegressionComparator().assertMatchesBaseline("""{"schemaVersion":2}""", baseline)
        } catch (ex: IllegalStateException) {
            assertThat(ex.message).contains("mismatch")
            return
        }
        throw AssertionError("expected mismatch")
    }
}
