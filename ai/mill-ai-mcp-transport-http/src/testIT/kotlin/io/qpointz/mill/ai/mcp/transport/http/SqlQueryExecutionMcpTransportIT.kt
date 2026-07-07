package io.qpointz.mill.ai.mcp.transport.http

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.spec.McpError
import io.modelcontextprotocol.spec.McpSchema
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest(
  classes = [McpSkymillSqlQueryExecutionITApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("sql-query-mcp-it")
class SqlQueryExecutionMcpTransportIT {

  companion object {
  private const val SKYMILL_CITIES_SQL =
    "SELECT `city` FROM `skymill`.`cities` LIMIT 1"

    @JvmStatic
    @DynamicPropertySource
    fun skymillSeeds(registry: DynamicPropertyRegistry) {
      val root = System.getProperty("flow.facet.it.root")
        ?: error("System property 'flow.facet.it.root' not set (expected from Gradle testIT)")
      registry.add("flow.facet.it.root") { root }
    }
  }

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
  fun shouldListTools_includingSqlQueryExecuteSql() {
    val tools = client.listTools().tools().map { it.name() }
    assertThat(tools).contains("sql-query.execute_sql")
  }

  @Test
  fun shouldListTools_includingSqlQueryDescribeSql() {
    val tools = client.listTools().tools().map { it.name() }
    assertThat(tools).contains("sql-query.describe_sql", "sql-query.validate_sql")
  }

  @Test
  fun shouldExecuteSql_andReturnStructuralResult() {
    val result = client.callTool(
      McpSchema.CallToolRequest.builder()
        .name("sql-query.execute_sql")
        .arguments(mapOf("sql" to SKYMILL_CITIES_SQL, "max_rows" to 5))
        .build(),
    )
    assertThat(result.isError).isNotEqualTo(true)
    val payload = parseToolJson(result)
    assertThat(payload["artifactType"].asText()).isEqualTo("sql-result")
    assertThat(payload["sql"].asText()).contains("cities")
    assertThat(payload["schema"].isArray).isTrue()
    assertThat(payload["schema"][0]["name"].asText()).isNotBlank
    assertThat(payload["schema"][0]["type"].asText()).isNotBlank
    assertThat(payload["rows"].isArray).isTrue()
    assertThat(payload["resultMode"].asText()).isEqualTo("paged")
    assertThat(payload["rowCount"].asInt()).isGreaterThanOrEqualTo(0)
  }

  @Test
  fun shouldDescribeSql_andReturnSchemaShape() {
    val result = client.callTool(
      McpSchema.CallToolRequest.builder()
        .name("sql-query.describe_sql")
        .arguments(mapOf("sql" to SKYMILL_CITIES_SQL))
        .build(),
    )
    assertThat(result.isError).isNotEqualTo(true)
    val payload = parseToolJson(result)
    assertThat(payload["artifactType"].asText()).isEqualTo("sql-description")
    assertThat(payload["schema"].isArray).isTrue()
    assertThat(payload["schema"][0]["type"].asText()).isNotBlank
    assertThat(payload["source"]["kind"].asText()).isEqualTo("execution")
    assertThat(payload["source"]["maxRows"].asInt()).isEqualTo(1)
    assertThat(payload.has("rows")).isFalse()
  }

  private fun parseToolJson(result: McpSchema.CallToolResult): JsonNode {
    val text = result.content().first() as McpSchema.TextContent
    return objectMapper.readTree(text.text())
  }
}

@SpringBootTest(
  classes = [McpSkymillSqlQueryExecutionITApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("sql-query-mcp-it")
@org.springframework.test.context.TestPropertySource(
  properties = [
    "mill.ai.mcp.profile=schema-exploration",
  ],
)
class SqlQueryExecutionMcpProfileRejectionIT {

  companion object {
    @JvmStatic
    @DynamicPropertySource
    fun skymillSeeds(registry: DynamicPropertyRegistry) {
      val root = System.getProperty("flow.facet.it.root")
        ?: error("System property 'flow.facet.it.root' not set (expected from Gradle testIT)")
      registry.add("flow.facet.it.root") { root }
    }
  }

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
  fun shouldRejectExecuteSql_outsideProfile() {
    val tools = client.listTools().tools().map { it.name() }
    assertThat(tools).noneMatch { it.startsWith("sql-query.") }
    assertThatThrownBy {
      client.callTool(
        McpSchema.CallToolRequest.builder()
          .name("sql-query.execute_sql")
          .arguments(mapOf("sql" to "SELECT 1"))
          .build(),
      )
    }.isInstanceOf(McpError::class.java)
  }
}
