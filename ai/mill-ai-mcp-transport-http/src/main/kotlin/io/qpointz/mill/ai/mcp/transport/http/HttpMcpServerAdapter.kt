package io.qpointz.mill.ai.mcp.transport.http

import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper
import io.modelcontextprotocol.server.McpServer
import io.modelcontextprotocol.server.McpSyncServer
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider
import io.modelcontextprotocol.spec.McpSchema
import io.modelcontextprotocol.spec.McpSchema.CallToolResult
import io.modelcontextprotocol.spec.McpSchema.Implementation
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities
import io.modelcontextprotocol.spec.McpSchema.Tool
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification
import io.qpointz.mill.ai.mcp.CapabilityMcpCatalog
import io.qpointz.mill.ai.mcp.CapabilityMcpExecutor
import io.qpointz.mill.ai.mcp.McpExposedTool
import io.qpointz.mill.ai.mcp.McpToolInvocationException
import org.slf4j.LoggerFactory

/**
 * MCP Java SDK server adapter backed by [CapabilityMcpCatalog] and [CapabilityMcpExecutor].
 *
 * @param catalog Exposed capability catalog.
 * @param executor Catalog-scoped tool executor.
 * @param endpoint Servlet mount path (for example `/services/mcp`).
 * @param objectMapper Jackson mapper shared with Mill.
 */
class HttpMcpServerAdapter(
  catalog: CapabilityMcpCatalog,
  private val executor: CapabilityMcpExecutor,
  endpoint: String,
  private val objectMapper: ObjectMapper = ObjectMapper(),
) : AutoCloseable {

  private val log = LoggerFactory.getLogger(HttpMcpServerAdapter::class.java)

  /** Servlet transport provider registered by Spring Boot. */
  val transportProvider: HttpServletStreamableServerTransportProvider

  private val server: McpSyncServer

  init {
    val jsonMapper = JacksonMcpJsonMapper(objectMapper)
    transportProvider = HttpServletStreamableServerTransportProvider.builder()
      .jsonMapper(jsonMapper)
      .mcpEndpoint(endpoint)
      .build()

    val toolSpecs = catalog.exposedTools.values
      .sortedBy { it.namespacedName }
      .map { exposed -> toSyncToolSpecification(exposed) }

    val serverBuilder = McpServer.sync(transportProvider)
      .serverInfo(Implementation("mill-ai-mcp", "1.0.0"))
      .capabilities(
        ServerCapabilities.builder()
          .tools(true)
          .resources(true, true)
          .prompts(true)
          .build(),
      )
      .tools(toolSpecs)

    server = serverBuilder.build()
    log.info("Mill MCP HTTP server started with {} tools at {}", catalog.exposedTools.size, endpoint)
  }

  private fun toSyncToolSpecification(exposed: McpExposedTool): SyncToolSpecification =
    SyncToolSpecification.builder()
      .tool(
        Tool.builder()
          .name(exposed.namespacedName)
          .description(exposed.description)
          .inputSchema(McpToolSchemaMapper.toMcpJsonSchema(exposed.inputSchema))
          .build(),
      )
      .callHandler { _, request ->
        try {
          val result = executor.callTool(exposed.namespacedName, request.arguments())
          CallToolResult.builder()
            .content(
              listOf(
                McpSchema.TextContent(objectMapper.writeValueAsString(result.content)),
              ),
            )
            .build()
        } catch (ex: McpToolInvocationException) {
          CallToolResult.builder()
            .isError(true)
            .content(listOf(McpSchema.TextContent(ex.message ?: "Tool invocation failed")))
            .build()
        }
      }
      .build()

  override fun close() {
    server.close()
  }
}
