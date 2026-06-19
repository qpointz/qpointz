package io.qpointz.mill.events.model

/**
 * Per-call options passed to [io.qpointz.mill.events.api.EventPublisher.publish].
 *
 * @property publishMode overrides the default publish mode for this single call
 */
data class PublishOptions(
    val publishMode: PublishMode = PublishMode.ASYNC,
)
