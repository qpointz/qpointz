package io.qpointz.mill.ai.runtime.langchain4j

import io.qpointz.mill.ai.core.capability.Capability
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.ai.runtime.TurnContextValues
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AnalysisTurnContextPromptTest {

    @Test
    fun shouldAppendAnalysisContextBlockForAnalysisCopilotProfile() {
        val profile = AgentProfile(id = "analysis-copilot", capabilityIds = setOf("conversation"))
        val prompt = buildAgentSystemPrompt(
            profile,
            emptyList<Capability>(),
            TurnContextValues(values = mapOf("sql.current" to "SELECT * FROM orders")),
        )

        assertThat(prompt).contains("analysis-copilot")
        assertThat(prompt).contains("SELECT * FROM orders")
        assertThat(prompt).contains("## Analysis context")
    }
}
