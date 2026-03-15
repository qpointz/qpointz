package io.qpointz.mill.ai

/**
 * Profile for the schema metadata-authoring agent.
 *
 * Composes five capabilities:
 * - `conversation` — base system prompt and streaming text protocol.
 * - `schema` — grounding tools: list_schemas, list_tables, list_columns, list_relations.
 * - `schema-authoring` — capture tools (capture_description, capture_relation) and the
 *   STRUCTURED_FINAL [schema-authoring.capture] protocol for terminal capture synthesis.
 * - `sql-dialect` — read-only SQL dialect conventions for SQL-writing agents.
 * - `sql-query` — SQL validation/execution tools and structured SQL/result-reference protocols.
 *
 * The planner grounds intent via the schema tools before calling a capture tool.
 * Once a capture tool completes the observer routes to synthesis via `schema-authoring.capture`.
 */
object SchemaAuthoringAgentProfile {
    val profile = AgentProfile(
        id = "schema-authoring",
        capabilityIds = setOf("conversation", "schema", "schema-authoring", "sql-dialect", "sql-query"),
    )
}
