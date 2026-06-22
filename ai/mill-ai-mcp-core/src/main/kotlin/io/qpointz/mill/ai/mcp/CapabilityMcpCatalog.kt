package io.qpointz.mill.ai.mcp

import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.capability.CapabilityRegistry
import io.qpointz.mill.ai.profile.AgentProfile

/**
 * Projects discovered capabilities into an MCP-consumable catalog using manifest metadata only.
 *
 * @param registry Capability provider registry.
 * @param exposureConfig Server allowlist configuration.
 * @param profile Optional profile filter.
 * @param admissionGate Admission gate (metadata for future enforcement).
 */
class CapabilityMcpCatalog(
    registry: CapabilityRegistry,
    exposureConfig: McpExposureConfig = McpExposureConfig(),
    profile: AgentProfile? = null,
    @Suppress("unused") admissionGate: CapabilityAdmissionGate = PermissiveAdmissionGate(),
    manifestLoader: (String) -> CapabilityManifest = CapabilityManifest::load,
) {
    private val index: McpExposureIndex = McpExposureIndex.build(
        registry = registry,
        exposureConfig = exposureConfig,
        profile = profile,
        manifestLoader = manifestLoader,
    )

    /** Exposed tools keyed by namespaced MCP tool name. */
    val exposedTools: Map<String, McpExposedTool> = index.exposedTools

    /** Self-describing resource descriptors for exposed assets. */
    val resourceDescriptors: List<ExternalCapabilityAssetDescriptor> = index.resourceDescriptors

    /** Shared exposure index for the matching executor. */
    internal val exposureIndex: McpExposureIndex = index

    /** Returns exposed tool names in stable order. */
    fun listToolNames(): List<String> = exposedTools.keys.sorted()
}
