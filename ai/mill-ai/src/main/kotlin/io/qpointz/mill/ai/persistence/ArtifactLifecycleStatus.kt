package io.qpointz.mill.ai.persistence

/**
 * Operator lifecycle for persisted facet-proposal chat artefacts.
 *
 * Facets are **included in writable scopes on capture** ([ACTIVE]). **Decline** excludes them;
 * **Accept** on a declined artefact re-includes them. Legacy [PENDING]/[ACCEPTED]/[RETRACTED]
 * values are still read from storage and normalised via [toWireStatus] / [isIncludedInScopes].
 */
enum class ArtifactLifecycleStatus {
    /** Included in writable metadata scopes (default on capture). */
    ACTIVE,

    /** Excluded from writable scopes; artefact remains visible on chat replay. */
    DECLINED,

    /** @deprecated Legacy default — treated as [ACTIVE] on the wire. */
    PENDING,

    /** @deprecated Legacy accept lock — treated as [ACTIVE] on the wire. */
    ACCEPTED,

    /** @deprecated Legacy reject — treated as [DECLINED] on the wire. */
    RETRACTED,
}

/** Whether scope rows for this artefact should be effective in rw scopes. */
fun ArtifactLifecycleStatus.isIncludedInScopes(): Boolean =
    when (this) {
        ArtifactLifecycleStatus.ACTIVE,
        ArtifactLifecycleStatus.PENDING,
        ArtifactLifecycleStatus.ACCEPTED,
        -> true
        ArtifactLifecycleStatus.DECLINED,
        ArtifactLifecycleStatus.RETRACTED,
        -> false
    }

/** Consumer-facing status on HTTP/SSE replay (`active` | `rejected`). */
fun ArtifactLifecycleStatus.toWireStatus(): String =
    when (this) {
        ArtifactLifecycleStatus.ACTIVE,
        ArtifactLifecycleStatus.PENDING,
        ArtifactLifecycleStatus.ACCEPTED,
        -> "active"
        ArtifactLifecycleStatus.DECLINED,
        ArtifactLifecycleStatus.RETRACTED,
        -> "rejected"
    }

/** True when the operator can decline (exclude from scopes). */
fun ArtifactLifecycleStatus.canDecline(): Boolean = isIncludedInScopes()

/** True when the operator can accept (re-include in scopes). */
fun ArtifactLifecycleStatus.canAccept(): Boolean = !isIncludedInScopes()
