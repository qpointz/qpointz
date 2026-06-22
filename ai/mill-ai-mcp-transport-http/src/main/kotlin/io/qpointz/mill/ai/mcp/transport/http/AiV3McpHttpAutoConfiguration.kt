package io.qpointz.mill.ai.mcp.transport.http

import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.dependencies.CapabilityDependencyAssembler
import io.qpointz.mill.ai.mcp.CapabilityMcpCatalog
import io.qpointz.mill.ai.mcp.CapabilityMcpExecutor
import io.qpointz.mill.ai.mcp.McpExposureConfig
import io.qpointz.mill.ai.mcp.transport.http.config.MillAiMcpProperties
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.profile.ProfileRegistry
import io.qpointz.mill.ai.runtime.AgentContext
import java.time.Instant
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean

/**
 * Registers the MCP Streamable HTTP servlet when {@code mill.ai.mcp.enabled=true}.
 */
@ConditionalOnAiEnabled
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "mill.ai.mcp", name = ["enabled"], havingValue = "true")
@AutoConfiguration
@AutoConfigureAfter(name = ["io.qpointz.mill.ai.autoconfigure.AiV3AutoConfiguration"])
@EnableConfigurationProperties(MillAiMcpProperties::class)
class AiV3McpHttpAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(CapabilityRegistry::class)
  fun capabilityRegistry(): CapabilityRegistry = CapabilityRegistry.load()

  @Bean
  fun capabilityMcpCatalog(
    registry: CapabilityRegistry,
    properties: MillAiMcpProperties,
    profileRegistry: ProfileRegistry,
  ): CapabilityMcpCatalog = CapabilityMcpCatalog(
    registry = registry,
    exposureConfig = McpExposureConfig(capabilities = properties.capabilities),
    profile = profileRegistry.resolve(properties.profile),
  )

  @Bean
  fun mcpAgentContext(
    properties: MillAiMcpProperties,
    profileRegistry: ProfileRegistry,
    dependencyAssembler: CapabilityDependencyAssembler,
  ): AgentContext {
    val profile = profileRegistry.resolve(properties.profile)
      ?: error("Unknown MCP profile '${properties.profile}'")
    val dependencies = dependencyAssembler.assemble(
      profile,
      ChatMetadata(
        chatId = "mcp",
        userId = "mcp",
        profileId = profile.id,
        chatName = "mcp",
        chatType = "mcp",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
      ),
    )
    return AgentContext(
      contextType = "general",
      capabilityDependencies = dependencies,
    )
  }

  @Bean
  fun capabilityMcpExecutor(
    registry: CapabilityRegistry,
    catalog: CapabilityMcpCatalog,
    mcpAgentContext: AgentContext,
  ): CapabilityMcpExecutor = CapabilityMcpExecutor(
    registry = registry,
    catalog = catalog,
    context = mcpAgentContext,
  )

  @Bean(destroyMethod = "close")
  fun httpMcpServerAdapter(
    catalog: CapabilityMcpCatalog,
    executor: CapabilityMcpExecutor,
    properties: MillAiMcpProperties,
    objectMapperProvider: ObjectProvider<ObjectMapper>,
  ): HttpMcpServerAdapter = HttpMcpServerAdapter(
    catalog = catalog,
    executor = executor,
    endpoint = properties.http.endpoint,
    objectMapper = objectMapperProvider.getIfAvailable { ObjectMapper() },
  )

  @Bean
  fun mcpServletRegistration(
    adapter: HttpMcpServerAdapter,
    properties: MillAiMcpProperties,
  ): ServletRegistrationBean<*> {
    val endpoint = properties.http.endpoint.trimEnd('/')
    return ServletRegistrationBean(adapter.transportProvider, endpoint, "$endpoint/", "$endpoint/*")
  }
}
