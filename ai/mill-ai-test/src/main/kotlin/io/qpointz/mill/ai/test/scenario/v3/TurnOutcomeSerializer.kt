package io.qpointz.mill.ai.test.scenario.v3

import io.qpointz.mill.ai.runtime.events.AgentEvent
import io.qpointz.mill.ai.sse.ChatSseEvent
import java.time.Instant

/**
 * Converts runtime types to stable JSON-friendly maps for regression records.
 */
object TurnOutcomeSerializer {

    /**
     * Serializes a [TurnOutcome] to a nested map suitable for JSON export.
     *
     * @param outcome Turn result to serialize.
     */
    fun toMap(outcome: TurnOutcome): Map<String, Any?> = mapOf(
        "response" to outcome.response,
        "events" to outcome.events.map { eventToMap(it) },
        "artifacts" to outcome.artifacts.map { artifactToMap(it) },
        "sseEvents" to outcome.sseEvents.map { sseToMap(it) },
        "structuredParts" to outcome.structuredParts.map {
            mapOf(
                "presentation" to it.presentation,
                "partType" to it.partType,
                "content" to it.content,
            )
        },
        "transcript" to outcome.transcript?.let { transcriptToMap(it) },
    )

    /**
     * Reconstructs a [TurnOutcome] from a saved record map (offline replay).
     *
     * @param map Outcome section from a regression record.
     */
    @Suppress("UNCHECKED_CAST")
    fun fromMap(map: Map<String, Any?>): TurnOutcome {
        val events = (map["events"] as? List<Map<String, Any?>>)?.map { mapToEvent(it) } ?: emptyList()
        val artifacts = (map["artifacts"] as? List<Map<String, Any?>>)?.map {
            ArtifactSnapshot(
                persistKind = it["persistKind"] as String,
                artifactId = it["artifactId"] as? String ?: "",
                payload = it["payload"] as? Map<String, Any?> ?: emptyMap(),
            )
        } ?: emptyList()
        return TurnOutcome(
            response = map["response"] as? String ?: "",
            events = events,
            artifacts = artifacts,
            transcript = (map["transcript"] as? Map<String, Any?>)?.let { transcriptFromMap(it) },
        )
    }

    private fun eventToMap(event: AgentEvent): Map<String, Any?> = when (event) {
        is AgentEvent.RunStarted -> mapOf("type" to event.type, "profileId" to event.profileId)
        is AgentEvent.ToolCall -> mapOf(
            "type" to event.type,
            "name" to event.name,
            "arguments" to event.arguments,
            "iteration" to event.iteration,
        )
        is AgentEvent.ToolResult -> mapOf("type" to event.type, "name" to event.name, "result" to event.result)
        is AgentEvent.ProtocolFinal -> mapOf("type" to event.type, "protocolId" to event.protocolId, "payload" to event.payload)
        is AgentEvent.AnswerCompleted -> mapOf("type" to event.type, "text" to event.text)
        is AgentEvent.LlmCallCompleted -> mapOf(
            "type" to event.type,
            "inputTokens" to event.inputTokens,
            "outputTokens" to event.outputTokens,
            "totalTokens" to event.totalTokens,
        )
        else -> mapOf("type" to event.type)
    }

    private fun mapToEvent(map: Map<String, Any?>): AgentEvent {
        return when (map["type"]) {
            "run.started" -> AgentEvent.RunStarted(map["profileId"] as String)
            "tool.call" -> AgentEvent.ToolCall(
                name = map["name"] as String,
                arguments = map["arguments"] as? Map<String, Any?> ?: emptyMap(),
                iteration = (map["iteration"] as? Number)?.toInt() ?: 0,
            )
            "tool.result" -> AgentEvent.ToolResult(
                name = map["name"] as String,
                result = map["result"],
            )
            "protocol.final" -> AgentEvent.ProtocolFinal(
                protocolId = map["protocolId"] as String,
                payload = map["payload"],
            )
            "answer.completed" -> AgentEvent.AnswerCompleted(map["text"] as? String ?: "")
            else -> AgentEvent.AnswerCompleted("")
        }
    }

    private fun artifactToMap(snapshot: ArtifactSnapshot): Map<String, Any?> = mapOf(
        "persistKind" to snapshot.persistKind,
        "artifactId" to snapshot.artifactId,
        "payload" to snapshot.payload,
    )

    private fun sseToMap(event: ChatSseEvent): Map<String, Any?> {
        val base = mutableMapOf<String, Any?>(
            "type" to event.type,
            "eventId" to event.eventId,
            "chatId" to event.chatId,
            "itemId" to event.itemId,
            "sequence" to event.sequence,
            "timestamp" to event.timestamp.toString(),
        )
        when (event) {
            is ChatSseEvent.ItemPartUpdated -> {
                base["presentation"] = event.presentation
                base["partType"] = event.partType
                base["content"] = event.content
            }
            is ChatSseEvent.ItemCompleted -> {
                base["presentation"] = event.presentation
                base["partType"] = event.partType
                base["content"] = event.content
            }
            else -> Unit
        }
        return base
    }

    private fun transcriptToMap(snapshot: TranscriptSnapshot): Map<String, Any?> = mapOf(
        "turnCount" to snapshot.turnCount,
        "turns" to snapshot.turns.map {
            mapOf("role" to it.role, "text" to it.text, "artifactIds" to it.artifactIds)
        },
        "activePointers" to snapshot.activePointers,
    )

    @Suppress("UNCHECKED_CAST")
    private fun transcriptFromMap(map: Map<String, Any?>): TranscriptSnapshot {
        val turns = (map["turns"] as? List<Map<String, Any?>>)?.map {
            TranscriptTurnSnapshot(
                role = it["role"] as String,
                text = it["text"] as? String,
                artifactIds = (it["artifactIds"] as? List<String>) ?: emptyList(),
            )
        } ?: emptyList()
        return TranscriptSnapshot(
            turnCount = (map["turnCount"] as? Number)?.toInt() ?: turns.size,
            turns = turns,
            activePointers = (map["activePointers"] as? Map<String, String>) ?: emptyMap(),
        )
    }
}
