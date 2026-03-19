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
 * Self-describing metadata used for discovery, composition, and future MCP exposure.
 */
data class CapabilityDescriptor(
    val id: String,
    val name: String,
    val description: String,
    val supportedContexts: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val requiredDependencies: Set<Class<out CapabilityDependency>> = emptySet(),
)





