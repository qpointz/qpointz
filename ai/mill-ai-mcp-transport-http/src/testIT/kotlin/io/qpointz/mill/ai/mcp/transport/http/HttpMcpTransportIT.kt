package io.qpointz.mill.ai.mcp.transport.http

import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper
import io.modelcontextprotocol.spec.McpError
import io.modelcontextprotocol.spec.McpSchema
import io.qpointz.mill.ai.mcp.CapabilityMcpCatalog
import io.qpointz.mill.service.descriptors.DescriptorTypes
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

/**
 * Shared helpers for embedded HTTP MCP integration tests (also used by WI-328 stdio bridge).
 */
object EmbeddedHttpMcpFixture {

  /**
   * Builds an MCP sync client against a running servlet endpoint.
   *
   * @param port Local server port.
   * @param objectMapper Shared Jackson mapper.
   * @param authorizationHeader Optional `Authorization` header value (for example `Basic …`).
   */
  fun syncClient(
    port: Int,
    objectMapper: ObjectMapper,
    authorizationHeader: String? = null,
  ): McpSyncClient {
    val transportBuilder = HttpClientStreamableHttpTransport.builder("http://localhost:$port")
      .endpoint("/services/mcp")
      .jsonMapper(JacksonMcpJsonMapper(objectMapper))
    if (authorizationHeader != null) {
      transportBuilder.customizeRequest { request -> request.header("Authorization", authorizationHeader) }
    }
    val transport = transportBuilder.build()
    return McpClient.sync(transport)
      .clientInfo(McpSchema.Implementation("mill-mcp-it", "1.0.0"))
      .build()
  }
}

@SpringBootTest(
  classes = [McpHttpTransportTestApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestPropertySource(
  properties = [
    "mill.ai.mcp.enabled=true",
    "mill.ai.mcp.profile=hello-world",
    "mill.security.enable=false",
  ],
)
class HttpMcpTransportIT {

  @Autowired
  private lateinit var catalog: CapabilityMcpCatalog

  @Autowired
  private lateinit var mcpServiceDescriptor: McpServiceDescriptor

  private val objectMapper = ObjectMapper()

  private var port: Int = 0

  @Autowired
  fun setPort(@org.springframework.boot.test.web.server.LocalServerPort localPort: Int) {
    port = localPort
  }

  private lateinit var client: McpSyncClient

  @BeforeEach
  fun setUp() {
    client = EmbeddedHttpMcpFixture.syncClient(port, objectMapper)
    client.initialize()
  }

  @AfterEach
  fun tearDown() {
    client.closeGracefully()
  }

  @Test
  fun shouldRegisterMcpServiceDescriptor_whenMcpEnabled() {
    assertThat(mcpServiceDescriptor.name).isEqualTo("ai-mcp")
    assertThat(mcpServiceDescriptor.typeName).isEqualTo(DescriptorTypes.SERVICE_TYPE_NAME)
  }

  @Test
  fun shouldListTools_includingDemoSayHello() {
    val tools = client.listTools().tools()
    assertThat(tools.map { it.name() }).contains("demo.say_hello")
  }

  @Test
  fun shouldCallDemoSayHello_andReturnGreeting() {
    val result = client.callTool(
      McpSchema.CallToolRequest.builder()
        .name("demo.say_hello")
        .arguments(mapOf("name" to "Mill"))
        .build(),
    )
    assertThat(result.isError).isNotEqualTo(true)
    val text = result.content().first() as McpSchema.TextContent
    assertThat(text.text()).contains("Hello, Mill!")
  }

  @Test
  fun shouldOmitDisabledCapability_fromToolList() {
    assertThat(catalog.listToolNames()).noneMatch { it.startsWith("test-mcp-disabled.") }
    val tools = client.listTools().tools()
    assertThat(tools.map { it.name() }).noneMatch { it.startsWith("test-mcp-disabled.") }
  }

  @Test
  fun shouldRejectToolCall_outsideProfile() {
    assertThat(client.listTools().tools().map { it.name() }).doesNotContain("schema.list_tables")
    assertThatThrownBy {
      client.callTool(
        McpSchema.CallToolRequest.builder()
          .name("schema.list_tables")
          .arguments(mapOf("schemaName" to "main"))
          .build(),
      )
    }.isInstanceOf(McpError::class.java)
  }
}
