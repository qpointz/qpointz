package io.qpointz.mill.ai

/**
 * Minimal protocol descriptor used to document capability-owned output/event contracts.
 */
data class ProtocolDefinition(
    val id: String,
    val description: String,
    val version: String = "1",
    val eventTypes: List<String> = emptyList(),
)
