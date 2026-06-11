package io.qpointz.mill.ai.test.runner

import io.qpointz.mill.ai.test.scenario.v3.ScriptStep

/**
 * FIFO queue of scripted model steps shared by the planner and protocol executor.
 */
class ScriptQueue(steps: List<ScriptStep>) {

    private val remaining = ArrayDeque(steps)

    /** Returns true when no scripted steps remain. */
    fun isEmpty(): Boolean = remaining.isEmpty()

    /**
     * Removes and returns the next script step.
     *
     * @param context Diagnostic context for exhaustion errors.
     */
    fun nextOrThrow(context: ScriptExhaustionContext): ScriptStep =
        remaining.removeFirstOrNull()
            ?: throw ScriptExhaustedException(context)
}

/**
 * Diagnostic context when the scripted model queue is exhausted.
 *
 * @param profileId Agent profile id.
 * @param turnIndex Zero-based turn index in the pack.
 * @param lastEventType Last agent event type emitted, if any.
 * @param scriptStepsTotal Total script steps declared for the turn.
 */
data class ScriptExhaustionContext(
    val profileId: String,
    val turnIndex: Int,
    val lastEventType: String?,
    val scriptStepsTotal: Int,
)

/**
 * Thrown when the agent or protocol executor needs another model call but the script is finished.
 */
class ScriptExhaustedException(
    val context: ScriptExhaustionContext,
) : IllegalStateException(
    "Script exhausted for profile=${context.profileId} turn=${context.turnIndex} " +
        "after ${context.scriptStepsTotal} steps; lastEvent=${context.lastEventType}",
)
