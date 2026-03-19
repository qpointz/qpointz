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
    val finalSchema: JsonObjectSchema? = null,
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





