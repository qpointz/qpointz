package io.qpointz.mill.ai.capabilities.concept

import io.qpointz.mill.ai.core.capability.Capability
import io.qpointz.mill.ai.core.capability.CapabilityDependencies
import io.qpointz.mill.ai.core.capability.CapabilityDescriptor
import io.qpointz.mill.ai.core.capability.CapabilityManifest
import io.qpointz.mill.ai.core.capability.CapabilityProvider
import io.qpointz.mill.ai.core.prompt.PromptAsset
import io.qpointz.mill.ai.core.protocol.ProtocolDefinition
import io.qpointz.mill.ai.core.tool.ToolBinding
import io.qpointz.mill.ai.core.tool.ToolResult
import io.qpointz.mill.ai.core.tool.argumentsAs
import io.qpointz.mill.ai.runtime.AgentContext

/** Provider for the read-only business concept capability. */
class ConceptCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "concept",
        name = "Concept",
        description = "Business concept catalog reads for model-level domain knowledge",
        supportedContexts = setOf("general"),
        tags = setOf("concept", "metadata"),
        requiredDependencies = setOf(ConceptCapabilityDependency::class.java),
    )

    override fun create(context: AgentContext, dependencies: CapabilityDependencies): Capability =
        ConceptCapability(
            descriptor(),
            context,
            dependencies.require(ConceptCapabilityDependency::class.java).catalog,
        )
}

private data class ListConceptsArgs(val tag: String? = null)

private data class GetConceptArgs(val conceptRef: String)

private data class SearchConceptsArgs(val query: String, val tag: String? = null)

private data class ConceptCapability(
    override val descriptor: CapabilityDescriptor,
    private val agentContext: AgentContext,
    private val catalog: ConceptCatalogPort,
) : Capability {

    private val manifest = CapabilityManifest.load("capabilities/concept.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = emptyList()

    private fun readScope(): String? = agentContext.readableScopesParam()

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("list_concept_tags") {
            ToolResult(catalog.listConceptTags(readScope()))
        },
        manifest.tool("list_concepts") { request ->
            val args = request.argumentsAs<ListConceptsArgs>()
            ToolResult(catalog.listConcepts(args.tag, readScope()))
        },
        manifest.tool("get_concept") { request ->
            val args = request.argumentsAs<GetConceptArgs>()
            val detail = try {
                catalog.getConcept(args.conceptRef, readScope())
            } catch (ex: IllegalArgumentException) {
                return@tool ToolResult(mapOf("error" to (ex.message ?: "invalid concept ref")))
            }
            ToolResult(detail ?: mapOf("error" to "concept not found: ${args.conceptRef}"))
        },
        manifest.tool("search_concepts") { request ->
            val args = request.argumentsAs<SearchConceptsArgs>()
            ToolResult(catalog.searchConcepts(args.query, args.tag, readScope()))
        },
        manifest.tool("get_model_concepts") {
            ToolResult(catalog.getModelConcepts(readScope()))
        },
    )
}
