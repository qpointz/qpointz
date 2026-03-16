package io.qpointz.mill.ai.capabilities.valuemapping

import io.qpointz.mill.ai.*
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingToolHandlers.getMappedAttributes
import io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingToolHandlers.resolveValues

/**
 * An attribute advertised by the value-mapping service for a given table.
 *
 * @property attribute Exact attribute (column) name.
 * @property mapped Whether this attribute has value mappings registered.
 */
data class MappedAttribute(val attribute: String, val mapped: Boolean)

/**
 * The result of resolving one requested value for a mapped attribute.
 *
 * @property requestedValue The user-facing term that was looked up.
 * @property mappedValue The canonical database value, or null if no match was found.
 */
data class ValueResolution(val requestedValue: String, val mappedValue: String?)

/**
 * Service boundary that [ValueMappingCapability] delegates to for all value-mapping queries.
 *
 * Implementations are provided by the caller (e.g. the agent entry point) and injected via
 * [ValueMappingCapabilityDependency]. This keeps the capability free from Spring and any
 * service-locator coupling.
 */
interface ValueMappingResolver {
    /**
     * Returns all attributes for [tableId] together with a flag that indicates whether
     * value mappings are registered for each attribute.
     *
     * Returns an empty list when [tableId] is unknown.
     */
    fun getMappedAttributes(tableId: String): List<MappedAttribute>

    /**
     * Resolves each of [requestedValues] to its canonical database value for the given
     * [tableId] and [attributeName].
     *
     * Entries whose term cannot be matched have [ValueResolution.mappedValue] set to null.
     */
    fun resolveValues(tableId: String, attributeName: String, requestedValues: List<String>): List<ValueResolution>
}

/**
 * Dependency carrying the [ValueMappingResolver] instance into [ValueMappingCapability].
 *
 * The resolver is injected rather than looked up directly because the capability is
 * instantiated per-run by the [CapabilityRegistry]. The registry delegates dependency
 * resolution to the caller, keeping the capability itself free from Spring coupling.
 */
data class ValueMappingCapabilityDependency(val resolver: ValueMappingResolver) : CapabilityDependency

/**
 * Provider for the value-mapping grounding capability.
 *
 * Declares [ValueMappingCapabilityDependency] as a required dependency so the registry
 * rejects profile configurations that forget to supply a [ValueMappingResolver].
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

/**
 * Value-mapping grounding capability.
 *
 * Contributes two grounding tools and a system prompt that instructs the planner to check
 * mapped attributes before writing SQL literals. Also declares the `value-mapping.result`
 * protocol for structured final output.
 *
 * Tool chain:
 * 1. `get_value_mapping_attributes` — discover which columns have mappings for a table.
 * 2. `get_value_mapping` — resolve user-provided terms to canonical database values.
 */
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

    override val tools: List<ToolDefinition> = listOf(
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
