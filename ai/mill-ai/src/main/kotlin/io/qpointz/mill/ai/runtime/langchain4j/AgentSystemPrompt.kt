package io.qpointz.mill.ai.runtime.langchain4j

import io.qpointz.mill.ai.core.capability.Capability
import io.qpointz.mill.ai.core.prompt.PromptAsset
import io.qpointz.mill.ai.profile.AgentProfile
import io.qpointz.mill.ai.runtime.TurnContextSanitizer
import io.qpointz.mill.ai.runtime.TurnContextValues

/**
 * Assembles the LLM system prompt from profile-scoped prompts (composed routing) and capability prompts.
 *
 * Profile prompts are emitted first so multi-capability profiles can declare non-overlapping intent
 * composition before capability-local guidance.
 */
internal fun buildAgentSystemPrompt(
    profile: AgentProfile,
    capabilities: List<Capability>,
    turnContext: TurnContextValues? = null,
): String {
    fun formatPrompt(prompt: PromptAsset): String =
        "## Prompt `${prompt.id}`\n${prompt.description}\n${prompt.content}"

    val profilePromptTexts = profile.prompts.map(::formatPrompt)
    val capabilityPromptTexts = capabilities.flatMap { capability ->
        capability.prompts.map(::formatPrompt)
    }
    return buildString {
        appendLine("You are the Mill AI agent '${profile.id}'.")
        appendLine("Keep replies concise.")
        appendLine()
        val all = profilePromptTexts + capabilityPromptTexts
        if (all.isNotEmpty()) appendLine(all.joinToString("\n\n"))
        if (profile.id == "analysis-copilot") {
            val analysisBlock = AnalysisTurnContextPrompt.format(TurnContextSanitizer.promptExcerpts(turnContext))
            if (analysisBlock != null) {
                appendLine()
                appendLine(analysisBlock)
            }
        }
    }
}
