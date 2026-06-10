package io.qpointz.mill.ai.runtime

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
 * Runtime context passed into capability/profile resolution.
 *
 * The initial hello-world flow uses only the coarse context type, but the same model leaves
 * room for future focused agents bound to a table, concept, or analysis object.
 */
data class AgentContext(
    val contextType: String,
    val focusEntityType: String? = null,
    val focusEntityId: String? = null,
    val capabilityDependencies: CapabilityDependencyContainer = CapabilityDependencyContainer.empty(),
)





