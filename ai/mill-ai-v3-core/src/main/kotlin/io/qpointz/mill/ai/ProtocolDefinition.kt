package io.qpointz.mill.ai

/**
 * Executable output contract for a capability-owned protocol.
 *
 * Validation rules:
 * - TEXT: finalSchema == null, events.isEmpty()
 * - STRUCTURED_FINAL: finalSchema != null
 * - STRUCTURED_STREAM: events.isNotEmpty()
 */
data class ProtocolDefinition(
    val id: String,
    val description: String,
    val version: String = "1",
    val mode: ProtocolMode,
    val fallbackMode: ProtocolMode? = null,
    val finalSchema: ToolSchema? = null,
    val events: List<ProtocolEventDefinition> = emptyList(),
) {
    init {
        when (mode) {
            ProtocolMode.TEXT -> {
                require(finalSchema == null) { "TEXT protocol must not declare finalSchema." }
                require(events.isEmpty()) { "TEXT protocol must not declare events." }
            }
            ProtocolMode.STRUCTURED_FINAL -> {
                require(finalSchema != null) { "STRUCTURED_FINAL protocol must declare finalSchema." }
            }
            ProtocolMode.STRUCTURED_STREAM -> {
                require(events.isNotEmpty()) { "STRUCTURED_STREAM protocol must declare at least one event." }
            }
        }
    }
}
