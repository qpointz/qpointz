package io.qpointz.mill.ai

/**
 * Passive capability package used by the runtime.
 *
 * A capability contributes prompts, tools, and protocols, but does not execute the workflow
 * itself.
 */
interface Capability {
    val descriptor: CapabilityDescriptor
    val prompts: List<PromptAsset>
    val tools: List<ToolBinding>
    val protocols: List<ProtocolDefinition>
}
