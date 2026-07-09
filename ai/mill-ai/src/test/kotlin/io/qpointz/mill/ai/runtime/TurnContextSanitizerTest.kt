package io.qpointz.mill.ai.runtime

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TurnContextSanitizerTest {

    @Test
    fun shouldIncludeBoundedPromptExcerptsOnly() {
        val turnContext = TurnContextValues(
            values = mapOf(
                "sql.current" to "SELECT 1",
                "artifact.query.name" to "Orders",
                "execution.last.rowCount" to 42,
                "execution.last.error" to "Syntax error",
                "custom.future.key" to true,
            ),
        )

        val excerpts = TurnContextSanitizer.promptExcerpts(turnContext)

        assertThat(excerpts.keys).containsExactlyInAnyOrder(
            "sql.current",
            "artifact.query.name",
            "execution.last.error",
        )
        assertThat(excerpts["sql.current"]).isEqualTo("SELECT 1")
    }

    @Test
    fun shouldReturnEmptyExcerptsForAbsentContext() {
        assertThat(TurnContextSanitizer.promptExcerpts(null)).isEmpty()
    }
}
