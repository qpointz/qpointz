package io.qpointz.mill.ai.core.artifact

import io.qpointz.mill.ai.core.protocol.ProtocolMode
import io.qpointz.mill.ai.runtime.events.routing.RoutedEventDestination

/**
 * Which runtime event type drives routing and persistence for an artefact descriptor.
 */
enum class ArtifactSourceEvent {
    /** Derived from [io.qpointz.mill.ai.runtime.events.AgentEvent.ToolResult] payload. */
    TOOL_RESULT,

    /** Derived from [io.qpointz.mill.ai.runtime.events.AgentEvent.ProtocolFinal]. */
    PROTOCOL_FINAL,
    ;

    companion object {
        /** Parses YAML `tool.result` / `protocol.final` values. */
        fun fromYaml(value: String): ArtifactSourceEvent = when (value.lowercase().replace('_', '.')) {
            "tool.result" -> TOOL_RESULT
            "protocol.final" -> PROTOCOL_FINAL
            else -> error("unsupported artifact sourceEvent: $value")
        }
    }
}

/**
 * How an artefact is produced at runtime.
 */
enum class EmissionStrategy {
    /** Coordinator constructs [io.qpointz.mill.ai.runtime.events.AgentEvent.ProtocolFinal] after tool success. */
    ON_TOOL_SUCCESS,

    /** [io.qpointz.mill.ai.runtime.langchain4j.LangChain4jProtocolExecutor] after CAPTURE tool success. */
    ON_CAPTURE_SUCCESS,

    /** Router maps tool-result payload only (no coordinator synthesis). */
    FROM_TOOL_RESULT,
    ;

    companion object {
        /** Parses YAML emission strategy names. */
        fun fromYaml(value: String): EmissionStrategy = when (value) {
            "OnToolSuccess" -> ON_TOOL_SUCCESS
            "OnCaptureSuccess" -> ON_CAPTURE_SUCCESS
            "FromToolResult" -> FROM_TOOL_RESULT
            else -> error("unsupported emissionStrategy: $value")
        }
    }
}

/**
 * Canonical artefact descriptor loaded from capability YAML `artifacts:` blocks.
 *
 * @param id Descriptor key within the owning capability (e.g. `generated-sql`).
 * @param capabilityId Owning capability id (e.g. `sql-query`).
 * @param protocolId Protocol id when [sourceEvent] is [ArtifactSourceEvent.PROTOCOL_FINAL].
 * @param artifactKind Logical kind in tool/protocol payloads (e.g. `generated-sql`).
 * @param persistKind Persistence bucket written by the projector (e.g. `sql.generated`).
 * @param pointerKeys Active pointer names updated when the artefact is persisted.
 * @param wirePartType SSE structured part type (e.g. `sql`, `facet-proposal`).
 * @param presentation SSE presentation (typically `structured`).
 * @param protocolMode Protocol mode when a protocol is involved.
 * @param sourceEvent Which runtime event drives routing for this descriptor.
 * @param emissionStrategy How the artefact is emitted at runtime.
 * @param destinations Routed destination lanes for persisted artefacts.
 */
data class ArtifactDescriptor(
    val id: String,
    val capabilityId: String,
    val protocolId: String? = null,
    val artifactKind: String,
    val persistKind: String,
    val pointerKeys: Set<String> = emptySet(),
    val wirePartType: String? = null,
    val presentation: String? = null,
    val protocolMode: ProtocolMode? = null,
    val sourceEvent: ArtifactSourceEvent,
    val emissionStrategy: EmissionStrategy,
    val destinations: Set<RoutedEventDestination>,
    /** When false, the artefact is live/SSE only and is not written to `ai_chat_artifact`. */
    val persist: Boolean = true,
)

/**
 * Tool-level emit trigger declared in capability YAML (`emitsOnSuccess`).
 *
 * @param toolName Tool handler name (e.g. `validate_sql`).
 * @param artifactId Target descriptor id within the same capability.
 * @param whenField Optional tool-result field to evaluate.
 * @param equals Expected value when [whenField] is set.
 */
data class ToolEmitTrigger(
    val toolName: String,
    val artifactId: String,
    val whenField: String? = null,
    val equals: Any? = null,
) {
    /**
     * Returns true when [toolResult] satisfies this trigger predicate.
     *
     * @param toolResult Structured tool result map.
     */
    fun matches(toolResult: Any?): Boolean {
        if (whenField == null) return true
        val map = structuredResultMap(toolResult) ?: return false
        val actual = map[whenField]
        return actual == equals
    }
}
