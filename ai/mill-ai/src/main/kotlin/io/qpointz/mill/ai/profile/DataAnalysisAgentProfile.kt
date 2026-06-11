package io.qpointz.mill.ai.profile

/**
 * Profile for natural-language data analysis (SQL generation without metadata authoring).
 *
 * Includes SQL validation and generated-SQL artefacts via the shared `sql-query` capability
 * descriptors, but excludes `metadata-authoring` / facet capture tools.
 */
object DataAnalysisAgentProfile {
    val profile = AgentProfile(
        id = "data-analysis",
        capabilityIds = setOf(
            "conversation",
            "schema",
            "metadata",
            "sql-dialect",
            "sql-query",
        ),
    )
}
