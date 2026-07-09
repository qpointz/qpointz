package io.qpointz.mill.ai.runtime.langchain4j

/**
 * Formats Analysis copilot turn context for the LLM system prompt.
 */
internal object AnalysisTurnContextPrompt {
    fun format(excerpts: Map<String, String>): String? {
        if (excerpts.isEmpty()) return null
        return buildString {
            appendLine("## Analysis context (current editor state)")
            excerpts["sql.current"]?.let { appendLine("Current SQL:").appendLine("```sql").appendLine(it).appendLine("```") }
            excerpts["artifact.query.name"]?.let { appendLine("Query name: $it") }
            excerpts["artifact.query.description"]?.let { appendLine("Query description: $it") }
            excerpts["execution.last.error"]?.let { appendLine("Last execution error: $it") }
        }.trim()
    }
}
