package io.qpointz.mill.ai

import dev.langchain4j.model.chat.request.json.JsonObjectSchema

data class ProtocolEventDefinition(
    val type: String,
    val description: String,
    val payloadSchema: JsonObjectSchema,
)
