package io.qpointz.mill.events.model

/**
 * Controls how a subscription handler is invoked relative to the dispatch thread.
 */
enum class ProcessingMode {
    /** Handler runs on a separate executor thread. */
    ASYNC,

    /** Handler runs to completion on the dispatch worker thread. */
    SYNC,

    /**
     * Handler runs after the current transaction commits.
     *
     * In the in-memory transport this degrades to [SYNC] semantics.
     * The Spring transport honours this via `@TransactionalEventListener(AFTER_COMMIT)`.
     */
    AFTER_COMMIT
}
