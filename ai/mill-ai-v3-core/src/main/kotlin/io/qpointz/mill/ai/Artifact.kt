package io.qpointz.mill.ai

/**
 * Placeholder for future persisted outputs produced by a run.
 *
 * The hello-world milestone does not persist artifacts yet, but this keeps the early state model
 * aligned with later chart/table/proposal producing agents.
 */
data class Artifact(
    val type: String,
    val ref: String,
)
