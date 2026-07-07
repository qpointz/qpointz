package io.qpointz.mill.ai.capabilities.chartmapping

import io.qpointz.mill.ai.capabilities.chartmapping.ChartMappingToolHandlers.listSupportedCharts
import io.qpointz.mill.ai.capabilities.chartmapping.ChartMappingToolHandlers.validateChartSpec
import io.qpointz.mill.ai.core.capability.Capability
import io.qpointz.mill.ai.core.capability.CapabilityDescriptor
import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.capability.CapabilityProvider
import io.qpointz.mill.ai.core.prompt.PromptAsset
import io.qpointz.mill.ai.core.protocol.ProtocolDefinition
import io.qpointz.mill.ai.core.tool.ToolBinding
import io.qpointz.mill.ai.core.tool.ToolResult
import io.qpointz.mill.ai.core.tool.argumentsAs
import io.qpointz.mill.ai.runtime.AgentContext

/**
 * Provider for the chart-mapping capability (catalog + chart validation only).
 */
class ChartMappingCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "chart-mapping",
        name = "Chart Mapping",
        description = "Chart catalog and visualization validation for sql.generated artifacts",
        supportedContexts = setOf("general"),
        tags = setOf("chart", "visualization"),
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability = ChartMappingCapability(descriptor())
}

private data class ChartMappingCapability(
    override val descriptor: CapabilityDescriptor,
) : Capability {

    private data class ListSupportedChartsArgs(
        val chartType: String? = null,
    )

    private data class ValidateChartSpecArgs(
        val schema: List<Map<String, Any?>>,
        val chartType: String,
        val encodings: Map<String, Any?>,
        val key: String? = null,
        val title: String? = null,
        val description: String? = null,
        val options: Map<String, Any?>? = null,
        val presentation: Map<String, Any?>? = null,
        val targetArtifactId: String? = null,
        val enrichExisting: Boolean = false,
    )

    private val manifest = CapabilityManifest.load("capabilities/chart-mapping.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("list_supported_charts") { request ->
            val args = request.argumentsAs<ListSupportedChartsArgs>()
            ToolResult(listSupportedCharts(chartType = args.chartType))
        },
        manifest.tool("validate_chart_spec") { request ->
            val args = request.argumentsAs<ValidateChartSpecArgs>()
            ToolResult(
                validateChartSpec(
                    schema = args.schema,
                    chartType = args.chartType,
                    encodings = args.encodings,
                    key = args.key ?: "default",
                    title = args.title,
                    description = args.description,
                    options = args.options,
                    presentation = args.presentation,
                    targetArtifactId = args.targetArtifactId,
                    enrichExisting = args.enrichExisting,
                ),
            )
        },
    )
}
