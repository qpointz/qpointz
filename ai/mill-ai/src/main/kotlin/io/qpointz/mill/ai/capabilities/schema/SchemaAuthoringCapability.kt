package io.qpointz.mill.ai.capabilities.schema

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
import io.qpointz.mill.metadata.domain.MetadataUrns

/**
 * Provider for the schema metadata-authoring capability.
 */
class SchemaAuthoringCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "schema-authoring",
        name = "Schema Authoring",
        description = "Metadata authoring capability: produces DescriptiveFacet and RelationFacet capture artifacts",
        supportedContexts = setOf("general"),
        tags = setOf("schema", "authoring"),
        requiredDependencies = setOf(SchemaCapabilityDependency::class.java),
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability = SchemaAuthoringCapability(
        descriptor(),
        dependencies.require(SchemaCapabilityDependency::class.java).catalog,
    )
}

/**
 * Canonical capture result returned by both [capture_description] and [capture_relation] tools.
 */
data class CaptureResult(
    val captureType: String,
    val targetEntityId: String,
    val targetEntityType: String,
    val serializedPayload: Map<String, Any?>,
    val validationWarnings: List<String> = emptyList(),
    /**
     * When `false`, the capture did not pass catalog checks (e.g. [targetEntityId] not found via
     * [SchemaCatalogPort]). The agent loop must not run terminal capture protocols for this result.
     * The assistant should **remediate** via schema tools + retry (see [resolverHint]).
     */
    val captureSucceeded: Boolean = true,
    /**
     * Populated when [captureSucceeded] is `false`. Instructs the model how to resolve ids and retry
     * (`list_schemas`, `list_tables`, `list_columns`, then capture again).
     */
    val resolverHint: String? = null,
)

/**
 * Schema metadata-authoring capability.
 *
 * Contributes two CAPTURE tools and a STRUCTURED_FINAL protocol for synthesis.
 */
private data class SchemaAuthoringCapability(
    override val descriptor: CapabilityDescriptor,
    private val catalog: SchemaCatalogPort,
) : Capability {

    private val manifest = CapabilityManifest.load("capabilities/schema-authoring.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts

    override val protocols: List<ProtocolDefinition> = manifest.allProtocols

    private data class CaptureDescriptionArgs(
        val targetEntityId: String,
        val targetEntityType: String,
        val description: String,
        val displayName: String? = null,
        val rationale: String? = null,
    )

    private data class CaptureRelationArgs(
        val relationName: String,
        val sourceTableId: String,
        val targetTableId: String,
        val sourceColumnIds: List<String> = emptyList(),
        val targetColumnIds: List<String> = emptyList(),
        val description: String? = null,
        val rationale: String? = null,
    )

    private data class RequestClarificationArgs(val question: String)

    override val tools: List<ToolBinding> = listOf(
        manifest.tool("request_clarification") { request ->
            val args = request.argumentsAs<RequestClarificationArgs>()
            ToolResult(mapOf("acknowledged" to true, "question" to args.question))
        },
        manifest.tool("capture_description", kindOverride = ToolKind.CAPTURE) { request ->
            val args = request.argumentsAs<CaptureDescriptionArgs>()
            val warnings = mutableListOf<String>()
            if (args.description.isBlank()) {
                warnings += "description is blank — the capture may produce an empty metadata entry"
            }
            val entityOk = canonicalEntityExists(catalog, args.targetEntityId, args.targetEntityType)
            val hint =
                if (entityOk) null
                else descriptionCaptureResolverHint()
            if (!entityOk) {
                warnings += entityResolutionCriticalMessage(args.targetEntityId, args.targetEntityType)
            }
            val payload = buildMap<String, Any?> {
                put("facetType", MetadataUrns.FACET_TYPE_DESCRIPTIVE)
                put("description", args.description)
                args.displayName?.let { put("displayName", it) }
                args.rationale?.let { put("rationale", it) }
            }
            ToolResult(
                CaptureResult(
                    captureType = "description",
                    targetEntityId = args.targetEntityId,
                    targetEntityType = args.targetEntityType,
                    serializedPayload = payload,
                    validationWarnings = warnings,
                    captureSucceeded = entityOk,
                    resolverHint = hint,
                )
            )
        },
        manifest.tool("capture_relation", kindOverride = ToolKind.CAPTURE) { request ->
            val args = request.argumentsAs<CaptureRelationArgs>()
            val warnings = mutableListOf<String>()
            if (args.sourceColumnIds.isEmpty() && args.targetColumnIds.isEmpty()) {
                warnings += "neither sourceColumnIds nor targetColumnIds were supplied — relation will be table-level only"
            }
            val relation = buildMap<String, Any?> {
                put("name", args.relationName)
                put("sourceTable", args.sourceTableId)
                put("targetTable", args.targetTableId)
                put("sourceColumns", args.sourceColumnIds)
                put("targetColumns", args.targetColumnIds)
                args.description?.let { put("description", it) }
            }
            val sourceOk =
                canonicalEntityExists(catalog, args.sourceTableId.trim(), SchemaEntityTypes.TABLE)
            val targetOk =
                canonicalEntityExists(catalog, args.targetTableId.trim(), SchemaEntityTypes.TABLE)
            val relOk = sourceOk && targetOk
            val hint = if (relOk) null else relationCaptureResolverHint()
            if (!sourceOk || !targetOk) {
                if (!sourceOk) warnings += entityResolutionCriticalMessage(args.sourceTableId.trim(), SchemaEntityTypes.TABLE)
                if (!targetOk) warnings += entityResolutionCriticalMessage(args.targetTableId.trim(), SchemaEntityTypes.TABLE)
            }
            val payload = buildMap<String, Any?> {
                put("facetType", MetadataUrns.FACET_TYPE_RELATION)
                put("relations", listOf(relation))
                args.rationale?.let { put("rationale", it) }
            }
            ToolResult(
                CaptureResult(
                    captureType = "relation",
                    targetEntityId = args.sourceTableId,
                    targetEntityType = "TABLE",
                    serializedPayload = payload,
                    validationWarnings = warnings,
                    captureSucceeded = relOk,
                    resolverHint = hint,
                )
            )
        },
    )
}

private object SchemaEntityTypes {
    const val SCHEMA = "SCHEMA"
    const val TABLE = "TABLE"
    const val COLUMN = "COLUMN"
}

/**
 * Validates [targetEntityId] against [catalog] for the stated [targetEntityType].
 *
 * Accepted **segment shapes** (dot-separated identifiers, identical to schema tool payloads):
 * - **SCHEMA** — one segment: `schemaName` (exactly one dot-free token)
 * - **TABLE** — two segments: `schemaName.tableName`
 * - **COLUMN** — three segments: `schemaName.tableName.columnName`
 */
private fun canonicalEntityExists(
    catalog: SchemaCatalogPort,
    entityIdRaw: String,
    targetEntityType: String,
): Boolean {
    val entityId = entityIdRaw.trim()
    val t = targetEntityType.trim().uppercase()
    return when (t) {
        SchemaEntityTypes.SCHEMA ->
            catalog.schemaCanonicalExists(entityId)
        SchemaEntityTypes.TABLE ->
            catalog.tableCanonicalExists(entityId)
        SchemaEntityTypes.COLUMN ->
            catalog.columnCanonicalExists(entityId)
        else ->
            false
    }
}

private fun SchemaCatalogPort.schemaCanonicalExists(id: String): Boolean {
    val parts = splitDottedFqn(id, expectedParts = 1) ?: return false
    val name = parts[0]
    return listSchemas().any { it.schemaName == name }
}

private fun SchemaCatalogPort.tableCanonicalExists(fqn: String): Boolean {
    val parts = splitCanonicalTable(fqn) ?: return false
    val (schema, table) = parts
    return listTables(schema).any { it.schemaName == schema && it.tableName == table }
}

private fun SchemaCatalogPort.columnCanonicalExists(fqn: String): Boolean {
    val parts = splitCanonicalColumn(fqn) ?: return false
    val (schema, table, column) = parts
    return listColumns(schema, table).any {
        it.schemaName == schema && it.tableName == table && it.columnName == column
    }
}

/** Parses `schema.table` (exactly two dotted segments); schema/table identifiers exclude `.`. */
private fun splitCanonicalTable(fqn: String): Pair<String, String>? {
    val parts = splitDottedFqn(fqn, expectedParts = 2) ?: return null
    return parts[0] to parts[1]
}

private fun splitCanonicalColumn(fqn: String): Triple<String, String, String>? {
    val parts = splitDottedFqn(fqn, expectedParts = 3) ?: return null
    return Triple(parts[0], parts[1], parts[2])
}

private fun splitDottedFqn(fqn: String, expectedParts: Int): List<String>? {
    val parts = fqn.split('.').map { it.trim() }.filter { it.isNotEmpty() }
    return if (parts.size == expectedParts) parts else null
}

private fun entityResolutionCriticalMessage(entityId: String, targetEntityType: String): String =
    """CRITICAL: targetEntityId "$entityId" did not resolve via schema tools for type $targetEntityType. """ +
        """Use exact ids from list_schemas / list_tables / list_columns: `schema`, `schema.table`, or `schema.table.column` — """ +
        """not display labels (e.g. "Cargo Clients")."""

/**
 * Returned on failed description captures — tells the planner to ground again and retry, not finish the turn.
 */
private fun descriptionCaptureResolverHint(): String =
    "captureSucceeded is false — re-ground ids with list_schemas, list_tables(schema), and optionally " +
        "list_columns(schema, table), then call capture_description again using exact schema/table/column names " +
        "from tool results only. Do not substitute display names. Repeat until captureSucceeded is true."

/**
 * Returned when source or target table ids fail validation for [capture_relation].
 */
private fun relationCaptureResolverHint(): String =
    "captureSucceeded is false — sourceTableId and targetTableId must each be schema.table from list_tables results. " +
        "Call list_tables as needed, then retry capture_relation with corrected ids."



