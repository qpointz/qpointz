package io.qpointz.mill.ai.tools

import com.fasterxml.jackson.databind.JsonNode
import io.qpointz.mill.utils.JsonUtils
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.ToolCallback
import org.springframework.ai.tool.function.FunctionToolCallback
import kotlin.jvm.java

object ProtocolSchema {

    private val log = LoggerFactory.getLogger(ProtocolSchema::class.java)

    data class CapabilityProtocolRequest(val capabilityName: String)

    data class CapabilityProtocolResponse(val capabilityName: String, val protocolJsonSchema:String?)

    private fun lookupProtocol(r: CapabilityProtocolRequest, schemas: Map<String, JsonNode?>): CapabilityProtocolResponse {
        val schema = schemas
            .getOrDefault(r.capabilityName, null)
            ?.let { JsonUtils.defaultJsonMapper().writeValueAsString(it) }
        log.info("Looking up protorocol {}:{}", r.capabilityName, schema)
        return CapabilityProtocolResponse(r.capabilityName, schema)
    }

    fun callback(schemas: Map<String, JsonNode?>): ToolCallback {
        return FunctionToolCallback
            .builder("capability_protocol_lookup",
                {k: CapabilityProtocolRequest-> lookupProtocol(k, schemas)})
            .description("Lookup capabiity protocol. Returns Protocol JSON Schema or null if protocol not defined")
            .inputType(CapabilityProtocolRequest::class.java)
            .build()
    }

}