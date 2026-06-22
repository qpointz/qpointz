package io.qpointz.mill.ai.mcp.transport.http

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.Base64
import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.client.McpSyncClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach

@SpringBootTest(
  classes = [McpHttpTransportTestApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureMockMvc
@TestPropertySource(
  properties = [
    "mill.ai.mcp.enabled=true",
    "mill.ai.mcp.profile=hello-world",
    "mill.security.enable=true",
    "mill.security.authentication.basic.enable=true",
    "mill.security.authentication.basic.store=classpath:passwd.yml",
  ],
)
class HttpMcpTransportSecurityIT {

  @Autowired
  private lateinit var mockMvc: MockMvc

  private var port: Int = 0

  @Autowired
  fun setPort(@org.springframework.boot.test.web.server.LocalServerPort localPort: Int) {
    port = localPort
  }

  private lateinit var client: McpSyncClient

  private val objectMapper = ObjectMapper()

  @BeforeEach
  fun setUp() {
    val token = Base64.getEncoder().encodeToString("usr1:password".toByteArray())
    client = EmbeddedHttpMcpFixture.syncClient(port, objectMapper, "Basic $token")
    client.initialize()
  }

  @AfterEach
  fun tearDown() {
    client.closeGracefully()
  }

  @Test
  fun shouldRequireAuthentication_whenSecurityEnabled() {
    mockMvc.perform(MockMvcRequestBuilders.post("/services/mcp"))
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun shouldAllowAuthenticatedMcpRequest_whenSecurityEnabled() {
    val tools = client.listTools().tools()
    assertThat(tools.map { it.name() }).contains("demo.say_hello")
  }
}
