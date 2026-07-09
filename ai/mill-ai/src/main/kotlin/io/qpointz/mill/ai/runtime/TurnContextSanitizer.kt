package io.qpointz.mill.ai.runtime

/**
 * Sanitizes ephemeral turn context before prompt injection.
 *
 * Unknown keys are retained on [TurnContextValues] but only a bounded subset is promoted to prompts.
 */
object TurnContextSanitizer {
    private const val MAX_VALUE_BYTES = 4096
    private const val MAX_SQL_CHARS = 16_384
    private const val MAX_NAME_CHARS = 200
    private const val MAX_DESCRIPTION_CHARS = 500
    private const val MAX_ERROR_CHARS = 500

    /**
     * Returns prompt-safe excerpts keyed by original context keys.
     */
    fun promptExcerpts(turnContext: TurnContextValues?): Map<String, String> {
        if (turnContext == null) return emptyMap()
        val out = linkedMapOf<String, String>()
        turnContext.stringValue("sql.current")?.let { sql ->
            out["sql.current"] = truncateChars(sql, MAX_SQL_CHARS)
        }
        turnContext.stringValue("artifact.query.name")?.let { name ->
            out["artifact.query.name"] = truncateChars(name, MAX_NAME_CHARS)
        }
        turnContext.stringValue("artifact.query.description")?.let { description ->
            out["artifact.query.description"] = truncateChars(description, MAX_DESCRIPTION_CHARS)
        }
        turnContext.stringValue("execution.last.error")?.let { error ->
            out["execution.last.error"] = truncateChars(error, MAX_ERROR_CHARS)
        }
        return out.filterValues { it.isNotBlank() }
    }

    private fun truncateChars(value: String, maxChars: Int): String {
        val bounded = if (value.length <= maxChars) value else value.take(maxChars)
        return truncateUtf8(bounded, MAX_VALUE_BYTES)
    }

    private fun truncateUtf8(value: String, maxBytes: Int): String {
        val bytes = value.toByteArray(Charsets.UTF_8)
        if (bytes.size <= maxBytes) return value
        var end = maxBytes
        while (end > 0 && (bytes[end - 1].toInt() and 0xC0) == 0x80) {
            end--
        }
        return String(bytes, 0, end, Charsets.UTF_8)
    }
}
