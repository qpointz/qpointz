package io.qpointz.mill.ai.valuemap

/**
 * Optional progress for [ValueMappingService.syncFromSource] / [VectorMappingSynchronizer.sync].
 * Persistence-free contract (WI-184 § Integration); orchestrator reduces before DB writes.
 */
interface ValueMappingSyncProgressCallback {

    /**
     * Called once after the post-dedupe work list is known and before processing the first element.
     */
    fun onBegin(attributeUrn: String, totalValues: Int)

    /**
     * Called after each logical value row is processed (embed + persist attempt).
     *
     * @param index zero-based index in the **deduped** entry list for this run
     */
    fun onElementProcessed(
        attributeUrn: String,
        index: Int,
        content: String,
        success: Boolean,
        detail: String?,
    )

    /**
     * Called when the run finishes (including when every element failed).
     */
    fun onRunComplete(attributeUrn: String, successCount: Int, failureCount: Int)
}
