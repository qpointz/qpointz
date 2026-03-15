package io.qpointz.mill.ai

data class ProtocolEventDefinition(
    val type: String,
    val description: String,
    val payloadSchema: ToolSchema,
)
