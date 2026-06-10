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
 * Profile for the schema metadata-authoring agent.
 *
 * Includes `metadata` facet catalog tools plus `metadata-authoring` capture for facet proposals.
 * `STRUCTURED_FINAL` artifact pointers for `schema-authoring.capture` / `metadata.faceting.capture`
 * are resolved by [io.qpointz.mill.ai.runtime.events.DefaultAgentEventRouter].
 */
object SchemaAuthoringAgentProfile {
    val profile = AgentProfile(
        id = "schema-authoring",
        capabilityIds = setOf(
            "conversation",
            "schema",
            "schema-authoring",
            "metadata",
            "metadata-authoring",
            "sql-dialect",
            "sql-query",
            "value-mapping",
        ),
    )
}
