package io.qpointz.mill.ai.core.protocol

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import dev.langchain4j.model.chat.request.json.JsonObjectSchema

data class ProtocolEventDefinition(
    val type: String,
    val description: String,
    val payloadSchema: JsonObjectSchema,
)





