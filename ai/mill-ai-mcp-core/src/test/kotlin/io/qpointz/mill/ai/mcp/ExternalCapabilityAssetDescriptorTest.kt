package io.qpointz.mill.ai.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import io.qpointz.mill.ai.core.tool.ToolKind
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExternalCapabilityAssetDescriptorTest {

    private val mapper = ObjectMapper()

    @Test
    fun shouldSerializeCapabilityDescriptor() {
        val descriptor = ExternalCapabilityAssetDescriptor.Capability(
            capabilityId = "demo",
            description = "Demo capability",
            tags = setOf("demo"),
        )
        val json = mapper.writeValueAsString(descriptor)
        assertThat(json).contains("\"capabilityId\":\"demo\"")
        assertThat(json).contains("\"assetKind\":\"capability\"")
        assertThat(descriptor.uri).isEqualTo(McpUriScheme.capability("demo"))
    }

    @Test
    fun shouldCarryToolKindAndSchemasOnToolDescriptor() {
        val descriptor = ExternalCapabilityAssetDescriptor.Tool(
            capabilityId = "schema",
            toolName = "list_tables",
            namespacedName = "schema.list_tables",
            toolKind = ToolKind.QUERY,
            inputSchema = JsonObjectSchema.builder().build(),
            outputSchema = null,
            description = "List tables",
        )
        assertThat(descriptor.assetKind).isEqualTo(ExternalCapabilityAssetDescriptor.ASSET_KIND_TOOL)
        assertThat(descriptor.toolKind).isEqualTo(ToolKind.QUERY)
        assertThat(descriptor.uri).isEqualTo(McpUriScheme.tool("schema", "list_tables"))
    }

    @Test
    fun shouldExposePromptProtocolAndArtifactVariants() {
        val prompt = ExternalCapabilityAssetDescriptor.Prompt(
            capabilityId = "demo",
            promptId = "demo.system",
            description = "System prompt",
        )
        val protocol = ExternalCapabilityAssetDescriptor.Protocol(
            capabilityId = "schema-authoring",
            protocolId = "authoring",
            mode = "FSM",
            description = "Authoring protocol",
        )
        val artifact = ExternalCapabilityAssetDescriptor.ArtifactSchema(
            capabilityId = "schema-authoring",
            artifactKind = "schema-description",
            description = "Schema description artifact",
        )
        assertThat(prompt.assetKind).isEqualTo(ExternalCapabilityAssetDescriptor.ASSET_KIND_PROMPT)
        assertThat(protocol.assetKind).isEqualTo(ExternalCapabilityAssetDescriptor.ASSET_KIND_PROTOCOL)
        assertThat(artifact.assetKind).isEqualTo(ExternalCapabilityAssetDescriptor.ASSET_KIND_ARTIFACT_SCHEMA)
    }
}
