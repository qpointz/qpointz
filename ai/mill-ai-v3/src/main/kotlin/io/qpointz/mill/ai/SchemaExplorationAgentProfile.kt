package io.qpointz.mill.ai

/**
 * Profile for the read-only schema exploration agent.
 *
 * Composes two capabilities:
 * - `conversation` — base system prompt and streaming text protocol (`conversation.stream`).
 * - `schema` — grounding tools: list_schemas, list_tables, list_columns, list_relations.
 *
 * This profile is intentionally exploration-only. It does not include `schema-authoring`,
 * so capture tools and STRUCTURED_FINAL protocols are unavailable. Use
 * [SchemaAuthoringAgentProfile] when the agent must also produce metadata capture artifacts.
 */
object SchemaExplorationAgentProfile {
    val profile = AgentProfile(
        id = "schema-exploration",
        capabilityIds = setOf("conversation", "schema"),
    )
}
