package io.qpointz.mill.ai.mcp.transport.http

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Well-known advertisement for the AI capability MCP HTTP surface when
 * {@code mill.ai.mcp.enabled=true}.
 *
 * <p>Connection hints ({@link DescriptorTypes#CONNECTIONS_TYPE_NAME}) are intentionally omitted —
 * MCP clients configure the Streamable HTTP URL directly; the {@code connections} bucket is reserved
 * for JDBC/Python data-plane discovery.
 */
@Component
@ConditionalOnAiEnabled
@ConditionalOnProperty(prefix = "mill.ai.mcp", name = ["enabled"], havingValue = "true")
class McpServiceDescriptor : Descriptor {

    /**
     * @return {@link DescriptorTypes#SERVICE_TYPE_NAME}
     */
    override fun getTypeName(): String = DescriptorTypes.SERVICE_TYPE_NAME

    /** Logical service id in discovery JSON (alongside {@code ai-chat}, {@code data-http}). */
    val name: String = "ai-mcp"
}
