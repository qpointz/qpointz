package io.qpointz.mill.ai.core.artifact

/**
 * Normalizes nested generated-sql payloads for chat wire (`sql` part type).
 */
object GeneratedSqlWire {

    /**
     * Flattens nested WI-366 payload for SSE/GET consumers while preserving all sections.
     *
     * @param payload one protocol-final item map
     * @return wire-ready map with top-level sql text plus nested sections
     */
    @Suppress("UNCHECKED_CAST")
    fun normalizeForWire(payload: Map<String, Any?>): Map<String, Any?> {
        val sqlSection = payload["sql"]
        val sqlText = when (sqlSection) {
            is Map<*, *> -> sqlSection["text"]?.toString().orEmpty()
            is String -> sqlSection
            else -> ""
        }
        return buildMap {
            put("sql", sqlText)
            when (sqlSection) {
                is Map<*, *> -> {
                    sqlSection["dialectId"]?.let { put("dialectId", it) }
                    sqlSection["statementKind"]?.let { put("statementKind", it) }
                }
                else -> {
                    payload["dialectId"]?.let { put("dialectId", it) }
                    payload["statementKind"]?.let { put("statementKind", it) }
                }
            }
            payload["info"]?.let { put("info", it) }
            payload["schema"]?.let { put("schema", it) }
            payload["visualizations"]?.let { put("visualizations", it) }
            payload["profiling"]?.let { put("profiling", it) }
            payload["artifactType"]?.let { put("artifactType", it) }
        }
    }
}
