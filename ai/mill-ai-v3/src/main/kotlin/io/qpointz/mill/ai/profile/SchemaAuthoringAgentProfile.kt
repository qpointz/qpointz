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
 * Composes five capabilities:
 * - `conversation` — base system prompt and streaming text protocol.
 * - `schema` — grounding tools: list_schemas, list_tables, list_columns, list_relations.
 * - `schema-authoring` — capture tools (capture_description, capture_relation) and the
 *   STRUCTURED_FINAL [schema-authoring.capture] protocol for terminal capture synthesis.
 * - `sql-dialect` — read-only SQL dialect conventions for SQL-writing agents.
 * - `sql-query` — SQL validation and generated-SQL protocols (execution is host-side).
 * - `value-mapping` — resolves user-facing terms to canonical database values before SQL generation.
 *
 * The planner grounds intent via the schema tools before calling a capture tool.
 * Once a capture tool completes the observer routes to synthesis via `schema-authoring.capture`.
 */
object SchemaAuthoringAgentProfile {
    private val routingPolicy = DefaultEventRoutingPolicy.policy.overriding(
        requireNotNull(DefaultEventRoutingPolicy.policy.ruleFor("protocol.final")) {
            "missing default rule for protocol.final"
        }.copy(
            artifactPointerKeys = setOf("last-schema-capture"),
        )
    )

    val profile = AgentProfile(
        id = "schema-authoring",
        capabilityIds = setOf("conversation", "schema", "schema-authoring", "sql-dialect", "sql-query", "value-mapping"),
        routingPolicy = routingPolicy,
    )
}





