package io.qpointz.mill.ai.capabilities.valuemapping

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingToolHandlers.getMappedAttributes
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingToolHandlers.resolveValues

/**
 * An attribute advertised by the value-mapping service for a given table.
 */
data class MappedAttribute(val attribute: String, val mapped: Boolean)

/**
 * The result of resolving one requested value for a mapped attribute.
 *
 * [ValueMappingResolver] implementations may leave [mappedValue] null when no mapping is found;
 * [io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingToolHandlers.resolveValues] normalizes
 * tool output by setting [mappedValue] to [requestedValue] in that case.
 *
 * @param similarityScore When the resolver uses vector similarity search, the score of the chosen match
 * (implementation-defined scale, often related to distance). Null when not applicable or not exposed.
 */
data class ValueResolution(
    val requestedValue: String,
    val mappedValue: String?,
    val similarityScore: Double? = null,
)

/**
 * Service boundary that [ValueMappingCapability] delegates to for all value-mapping queries.
 */
interface ValueMappingResolver {
    fun getMappedAttributes(tableId: String): List<MappedAttribute>
    fun resolveValues(tableId: String, attributeName: String, requestedValues: List<String>): List<ValueResolution>
}

/**
 * Dependency carrying the [ValueMappingResolver] instance into [ValueMappingCapability].
 */
data class ValueMappingCapabilityDependency(val resolver: ValueMappingResolver) : CapabilityDependency

/**
 * Provider for the value-mapping grounding capability.
 */
class ValueMappingCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "value-mapping",
        name = "Value Mapping",
        description = "Resolves user-facing terms to canonical database values for mapped attributes",
        supportedContexts = setOf("general"),
        tags = setOf("value-mapping", "normalization"),
        requiredDependencies = setOf(ValueMappingCapabilityDependency::class.java),
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability = ValueMappingCapability(
        descriptor(),
        dependencies.require(ValueMappingCapabilityDependency::class.java).resolver,
    )
}

private data class ValueMappingCapability(
    override val descriptor: CapabilityDescriptor,
    private val resolver: ValueMappingResolver,
) : Capability {

    private data class GetValueMappingAttributesArgs(val table: String)

    private data class GetValueMappingArgs(
        val table: String,
        val attribute: String,
        val values: List<String>,
    )

    private val manifest = CapabilityManifest.load("capabilities/value-mapping.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("get_value_mapping_attributes") { request ->
            val args = request.argumentsAs<GetValueMappingAttributesArgs>()
            ToolResult(getMappedAttributes(resolver, args.table))
        },
        manifest.tool("get_value_mapping") { request ->
            val args = request.argumentsAs<GetValueMappingArgs>()
            ToolResult(resolveValues(resolver, args.table, args.attribute, args.values))
        },
    )
}




