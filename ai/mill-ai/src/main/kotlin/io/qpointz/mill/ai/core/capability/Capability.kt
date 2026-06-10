package io.qpointz.mill.ai.core.capability

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





