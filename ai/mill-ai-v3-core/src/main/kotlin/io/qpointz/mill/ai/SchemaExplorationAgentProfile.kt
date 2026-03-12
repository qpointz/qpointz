package io.qpointz.mill.ai

/**
 * Profile for the schema exploration agent.
 *
 * Composes the conversation capability (system prompt) with the schema capability
 * (list_schemas, list_tables, list_columns, list_relations).
 */
object SchemaExplorationAgentProfile {
    val profile = AgentProfile(
        id = "schema-exploration",
        capabilityIds = setOf("conversation", "schema"),
    )
}
