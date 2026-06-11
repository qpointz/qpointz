package io.qpointz.mill.ai.test.scenario.v3.checks

import io.qpointz.mill.ai.runtime.events.AgentEvent
import io.qpointz.mill.ai.sse.ChatSseEvent
import io.qpointz.mill.ai.test.scenario.v3.ArtifactSnapshot
import io.qpointz.mill.ai.test.scenario.v3.CheckResult
import io.qpointz.mill.ai.test.scenario.v3.CheckStatus
import io.qpointz.mill.ai.test.scenario.v3.TurnCheck
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome

@Suppress("UNCHECKED_CAST")
private fun Any?.asMap(): Map<String, Any?> = this as? Map<String, Any?> ?: emptyMap()

@Suppress("UNCHECKED_CAST")
private fun Any?.asListOfMaps(): List<Map<String, Any?>> =
    (this as? List<*>)?.map { it.asMap() } ?: emptyList()

/** Asserts runtime event order and shape. */
class EventsTurnCheck : TurnCheck {
    override val type: String = "events"

    override fun run(outcome: TurnOutcome, spec: Any?): CheckResult {
        val body = spec.asMap()
        val expected = body["containsInOrder"]?.let { asListOfMaps(it) } ?: return fail("events check requires containsInOrder")
        val actual = outcome.events.map { it.toCheckMap() }
        var searchFrom = 0
        for (pattern in expected) {
            val matchIndex = (searchFrom until actual.size).firstOrNull { matchesEvent(pattern, actual[it]) }
            if (matchIndex == null) {
                return fail("expected event $pattern not found after index $searchFrom in $actual")
            }
            searchFrom = matchIndex + 1
        }
        return pass()
    }

    private fun asListOfMaps(value: Any?): List<Map<String, Any?>> =
        (value as? List<*>)?.map { (it as? Map<*, *>)?.mapKeys { e -> e.key.toString() }?.mapValues { e -> e.value } ?: emptyMap() }
            ?: emptyList()

    private fun matchesEvent(pattern: Map<String, Any?>, event: Map<String, Any?>): Boolean =
        pattern.all { (key, expected) -> event[key] == expected }

    private fun AgentEvent.toCheckMap(): Map<String, Any?> = when (this) {
        is AgentEvent.RunStarted -> mapOf("type" to type, "profileId" to profileId)
        is AgentEvent.ToolCall -> mapOf("type" to type, "name" to name)
        is AgentEvent.ToolResult -> mapOf("type" to type, "name" to name)
        is AgentEvent.ProtocolFinal -> mapOf("type" to type, "protocolId" to protocolId)
        is AgentEvent.AnswerCompleted -> mapOf("type" to type)
        else -> mapOf("type" to type)
    }
}

/** Asserts persisted artefact counts by kind. */
class ArtifactsTurnCheck : TurnCheck {
    override val type: String = "artifacts"

    override fun run(outcome: TurnOutcome, spec: Any?): CheckResult {
        val rules = when (spec) {
            is List<*> -> spec.map { it.asMap() }
            is Map<*, *> -> listOf(spec.asMap())
            else -> return fail("artifacts check must be a list or map")
        }
        for (rule in rules) {
            val kind = rule["persistKind"]?.toString()
                ?: return fail("artifacts rule requires persistKind")
            val expectedCount = (rule["count"] as? Number)?.toInt() ?: 1
            val actual = outcome.artifacts.count { it.persistKind == kind }
            if (actual != expectedCount) {
                return fail("persistKind=$kind expected count $expectedCount but was $actual")
            }
        }
        return pass()
    }
}

/** Asserts mapped SSE events. */
class SseTurnCheck : TurnCheck {
    override val type: String = "sse"

    override fun run(outcome: TurnOutcome, spec: Any?): CheckResult {
        val rules = when (spec) {
            is List<*> -> spec.map { it.asMap() }
            is Map<*, *> -> listOf(spec.asMap())
            else -> return fail("sse check must be a list or map")
        }
        for (rule in rules) {
            val typeFilter = rule["type"]?.toString()
            val presentation = rule["presentation"]?.toString()
            val partType = rule["partType"]?.toString()
            val found = outcome.sseEvents.any { event ->
                matchesSse(event, typeFilter, presentation, partType)
            }
            if (!found) {
                return fail("no SSE event matching $rule in ${outcome.sseEvents.map { it.type }}")
            }
        }
        return pass()
    }

    private fun matchesSse(
        event: ChatSseEvent,
        type: String?,
        presentation: String?,
        partType: String?,
    ): Boolean {
        if (type != null && event.type != type) return false
        if (event is ChatSseEvent.ItemPartUpdated) {
            if (presentation != null && event.presentation != presentation) return false
            if (partType != null && event.partType != partType) return false
        }
        return true
    }
}

/** Asserts final assistant response text. */
class ResponseTurnCheck : TurnCheck {
    override val type: String = "response"

    override fun run(outcome: TurnOutcome, spec: Any?): CheckResult {
        val body = spec.asMap()
        body["contains"]?.toString()?.let { substring ->
            return if (outcome.response.contains(substring, ignoreCase = true)) pass()
            else fail("response does not contain: $substring")
        }
        return when (body["assert"]?.toString()) {
            "not-blank" ->
                if (outcome.response.isNotBlank()) pass()
                else fail("response was blank")
            else -> fail("unsupported response check: $body")
        }
    }
}

/** Asserts durable transcript shape. */
class TranscriptTurnCheck : TurnCheck {
    override val type: String = "transcript"

    override fun run(outcome: TurnOutcome, spec: Any?): CheckResult {
        val body = spec.asMap()
        val expectedTurns = (body["turnCount"] as? Number)?.toInt()
        val transcript = outcome.transcript
            ?: return fail("no transcript in outcome")
        if (expectedTurns != null && transcript.turnCount != expectedTurns) {
            return fail("expected turnCount=$expectedTurns but was ${transcript.turnCount}")
        }
        return pass()
    }
}

private fun pass() = CheckResult(CheckStatus.PASS)

private fun fail(detail: String) = CheckResult(CheckStatus.FAIL, detail)
