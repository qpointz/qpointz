package io.qpointz.mill.ai.runtime

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

/**
 * Runtime-owned state for a single agent run.
 *
 * This state is intentionally framework-free and small enough for early agents, while being rich
 * enough to support explicit tool tracking and future durable persistence.
 */
data class RunState(
    val profile: AgentProfile,
    val context: AgentContext,
    val runId: String? = null,
    val conversationId: String? = null,
    val steps: List<RunStep> = emptyList(),
    val artifacts: List<ArtifactRecord> = emptyList(),
    val clarificationCount: Int = 0,
    val toolCallCount: Int = 0,
) {
    val iteration: Int
        get() = steps.size

    fun appendStep(step: RunStep): RunState = copy(
        steps = steps + step,
        clarificationCount = clarificationCount + if (step.kind == RunStepKind.CLARIFICATION) 1 else 0,
        toolCallCount = toolCallCount + if (step.kind == RunStepKind.TOOL_CALL) 1 else 0,
    )

    fun addArtifact(artifact: ArtifactRecord): RunState = copy(artifacts = artifacts + artifact)
}

data class RunStep(
    val kind: RunStepKind,
    val toolName: String? = null,
    val toolResult: String? = null,
    val summary: String? = null,
    val selectedProtocolId: String? = null,
    val protocolPayload: String? = null,
)

enum class RunStepKind {
    PLAN,
    TOOL_CALL,
    SYNTHESIS,
    CLARIFICATION,
    FAILURE,
}





