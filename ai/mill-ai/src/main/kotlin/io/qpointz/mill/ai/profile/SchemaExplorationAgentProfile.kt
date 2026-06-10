package io.qpointz.mill.ai.profile

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
 * Profile for the read-only schema exploration agent.
 *
 * Composes: `conversation`, `schema` grounding tools, and **`metadata`** read-only facet catalog tools.
 * Does **not** include capture capabilities (`schema-authoring`, `metadata-authoring`).
 */
object SchemaExplorationAgentProfile {
    val profile = AgentProfile(
        id = "schema-exploration",
        capabilityIds = setOf("conversation", "schema", "metadata"),
    )
}
