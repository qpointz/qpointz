package io.qpointz.mill.ai.persistence

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

import java.time.Instant

/**
 * Durable record of a single routed event persisted during a run.
 *
 * Not every event is persisted; the [EventRoutingRule.persistEvent] flag governs which are.
 */
data class RunEventRecord(
    val eventId: String,
    val runId: String,
    val conversationId: String?,
    val profileId: String,
    /** Stable routed kind label. */
    val kind: String,
    /** Raw source event type. */
    val runtimeType: String,
    /** Structured event payload. */
    val content: Map<String, Any?>,
    val createdAt: Instant,
)

/**
 * Port for persisted run-level events (telemetry, plan decisions, run lifecycle).
 */
interface RunEventStore {
    fun save(record: RunEventRecord)
    fun findByRun(runId: String): List<RunEventRecord>
}





