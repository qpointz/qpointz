package io.qpointz.mill.ai.autoconfigure.chat

import io.qpointz.mill.ai.chat.ChatRuntimeEvent
import io.qpointz.mill.ai.core.artifact.ArtifactDescriptorRegistry
import io.qpointz.mill.ai.runtime.events.AgentEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

class LangChain4jChatRuntimeStructuredTest {

    private val registry = ArtifactDescriptorRegistry.loadDefault()
    private val jsonMapper = JsonMapper.builder().addModule(kotlinModule()).build()

    @Test
    fun shouldMapSqlProtocolFinal_toStructuredSqlPart() {
        val event = AgentEvent.ProtocolFinal(
            protocolId = "sql-query.generated-sql",
            payload = mapOf("artifactType" to "generated-sql", "sql" to "SELECT 1"),
        )
        val part = toStructuredPart(event)
        assertThat(part).isNotNull
        assertThat(part!!.presentation).isEqualTo("structured")
        assertThat(part.partType).isEqualTo("sql")
        assertThat(part.mode).isEqualTo("replace")
        assertThat(part.content).contains("SELECT 1")
    }

    @Test
    fun shouldMapFacetProtocolFinal_toStructuredFacetProposalPart() {
        val event = AgentEvent.ProtocolFinal(
            protocolId = "metadata.faceting.capture",
            payload = mapOf("captureType" to "facet-proposal"),
        )
        val part = toStructuredPart(event)
        assertThat(part).isNotNull
        assertThat(part!!.partType).isEqualTo("facet-proposal")
    }

    /** Mirrors [LangChain4jChatRuntime] registry-driven SSE bridge mapping. */
    private fun toStructuredPart(event: AgentEvent.ProtocolFinal): ChatRuntimeEvent.StructuredPart? {
        val descriptor = registry.descriptorForProtocol(event.protocolId) ?: return null
        val wirePartType = descriptor.wirePartType ?: return null
        val presentation = descriptor.presentation ?: "structured"
        val json = when (val payload = event.payload) {
            null -> "{}"
            is String -> payload
            else -> jsonMapper.writeValueAsString(payload)
        }
        return ChatRuntimeEvent.StructuredPart(
            presentation = presentation,
            partType = wirePartType,
            mode = "replace",
            content = json,
        )
    }
}
