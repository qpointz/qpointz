package io.qpointz.mill.ai.test.scenario.v3

import io.qpointz.mill.ai.runtime.events.AgentEvent
import io.qpointz.mill.ai.sse.ChatSseEvent

/**
 * Full result bag for a single scenario turn — everything [TurnCheck] implementations may assert on.
 *
 * @param response Final assistant text returned by the agent run.
 * @param events Raw runtime [AgentEvent] list in emission order.
 * @param artifacts Persisted artefact snapshots (kind + payload).
 * @param sseEvents Public SSE events mapped from agent events for this turn.
 * @param structuredParts Structured presentation slices (subset of SSE for convenience).
 * @param transcript Durable conversation transcript snapshot after the turn.
 */
data class TurnOutcome(
    val response: String,
    val events: List<AgentEvent>,
    val artifacts: List<ArtifactSnapshot> = emptyList(),
    val sseEvents: List<ChatSseEvent> = emptyList(),
    val structuredParts: List<StructuredPartSnapshot> = emptyList(),
    val transcript: TranscriptSnapshot? = null,
)

/**
 * Persisted artefact slice for scenario checks and regression records.
 *
 * @param persistKind Routing/persistence kind (e.g. `sql.generated`).
 * @param artifactId Durable artefact id (scrubbed in normalized baselines).
 * @param payload Structured artefact payload.
 */
data class ArtifactSnapshot(
    val persistKind: String,
    val artifactId: String,
    val payload: Map<String, Any?>,
)

/**
 * Structured SSE part snapshot for shape checks.
 *
 * @param presentation SSE presentation (e.g. `structured`).
 * @param partType Wire part type (e.g. `sql`).
 * @param content Parsed or raw content map/string.
 */
data class StructuredPartSnapshot(
    val presentation: String,
    val partType: String,
    val content: Any?,
)

/**
 * Transcript snapshot for multi-turn scenarios.
 *
 * @param turnCount Number of turns in the durable transcript.
 * @param turns Simplified turn rows for regression diff.
 * @param activePointers Active artefact pointer map after the turn.
 */
data class TranscriptSnapshot(
    val turnCount: Int,
    val turns: List<TranscriptTurnSnapshot> = emptyList(),
    val activePointers: Map<String, String> = emptyMap(),
)

/**
 * Single transcript turn row in a regression record.
 *
 * @param role `user` or `assistant`.
 * @param text Turn text, if any.
 * @param artifactIds Linked artefact ids.
 */
data class TranscriptTurnSnapshot(
    val role: String,
    val text: String?,
    val artifactIds: List<String> = emptyList(),
)
