package io.qpointz.mill.ai

/**
 * Typed runtime events emitted by the hello-world agent.
 *
 * These are runtime-owned events; the LLM does not generate them directly.
 */
sealed interface AgentEvent {
    val type: String

    /** Marks the start of a single agent run for a resolved profile. */
    data class RunStarted(
        val profileId: String,
    ) : AgentEvent {
        override val type: String = "run.started"
    }

    /** Short user-visible progress update emitted between concrete actions. */
    data class ThinkingDelta(
        val message: String,
    ) : AgentEvent {
        override val type: String = "thinking.delta"
    }

    /** Emits the structured decision chosen by the planner for the next step. */
    data class PlanCreated(
        val mode: String,
        val toolName: String? = null,
    ) : AgentEvent {
        override val type: String = "plan.created"
    }

    /** Partial answer text streamed directly from the model. */
    data class MessageDelta(
        val text: String,
    ) : AgentEvent {
        override val type: String = "message.delta"
    }

    /** Declares that the model requested a concrete tool execution. */
    data class ToolCall(
        val name: String,
        val arguments: String,
        val iteration: Int,
    ) : AgentEvent {
        override val type: String = "tool.call"
    }

    /** Carries the serialized result returned by a tool invocation. */
    data class ToolResult(
        val name: String,
        val result: String,
    ) : AgentEvent {
        override val type: String = "tool.result"
    }

    /** Summarizes the observer decision after a step finishes. */
    data class ObservationMade(
        val decision: String,
        val reason: String,
    ) : AgentEvent {
        override val type: String = "observation.made"
    }

    /** Final assembled answer for the turn after any tool work completes. */
    data class AnswerCompleted(
        val text: String,
    ) : AgentEvent {
        override val type: String = "answer.completed"
    }

    /** Streamed reasoning / extended-thinking token from the model (when the model supports it). */
    data class ReasoningDelta(
        val text: String,
    ) : AgentEvent {
        override val type: String = "reasoning.delta"
    }

    /** Partial text token streamed via a TEXT-mode protocol. */
    data class ProtocolTextDelta(
        val protocolId: String,
        val text: String,
    ) : AgentEvent {
        override val type: String = "protocol.text.delta"
    }

    /** Final validated payload emitted by a STRUCTURED_FINAL-mode protocol. */
    data class ProtocolFinal(
        val protocolId: String,
        val payload: String,
    ) : AgentEvent {
        override val type: String = "protocol.final"
    }

    /** Single validated event emitted by a STRUCTURED_STREAM-mode protocol. */
    data class ProtocolStreamEvent(
        val protocolId: String,
        val eventType: String,
        val payload: String,
    ) : AgentEvent {
        override val type: String = "protocol.stream.event"
    }
}
