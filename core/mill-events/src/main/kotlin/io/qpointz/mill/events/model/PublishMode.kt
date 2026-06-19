package io.qpointz.mill.events.model

/**
 * Controls whether [io.qpointz.mill.events.api.EventTransport.publish] returns immediately
 * or blocks until dispatch completes.
 */
enum class PublishMode {
    /** Returns immediately after accepting the event for dispatch. */
    ASYNC,

    /** Blocks until all handlers for the event have been invoked. */
    SYNC
}
