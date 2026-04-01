package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.metadata.source.MetadataOriginIds
import java.time.Instant

/**
 * Unified read model for one effective facet row (captured or inferred).
 *
 * Carries persisted row fields when [origin] is [FacetOrigin.CAPTURED] plus provenance
 * ([origin], [originId], [assignmentUid]) for API / UI.
 *
 * @property assignmentUuid when [FacetOrigin.CAPTURED], equals persisted assignment [FacetAssignment.uid]; may differ for inferred rows
 * @property assignmentUid stable id for captured rows (duplicate of [assignmentUuid] today); null when not assignment-backed
 * @property originId contributing source, e.g. [MetadataOriginIds.REPOSITORY_LOCAL]
 */
data class FacetInstance(
    val assignmentUuid: String,
    val entityId: String,
    val facetTypeKey: String,
    val scopeKey: String,
    val mergeAction: MergeAction,
    val payload: Map<String, Any?>,
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?,
    val origin: FacetOrigin,
    val originId: String,
    val assignmentUid: String?
) {
    /** Stable external id for paths and DTOs; equals [assignmentUuid]. */
    val uid: String get() = assignmentUuid
}

/**
 * Maps a persisted assignment to a captured read row for the repository [MetadataSource].
 *
 * @param repositoryOriginId value of [io.qpointz.mill.metadata.source.RepositoryMetadataSource.originId]
 */
fun FacetAssignment.toCapturedReadModel(repositoryOriginId: String = MetadataOriginIds.REPOSITORY_LOCAL): FacetInstance =
    FacetInstance(
        assignmentUuid = uid,
        entityId = entityId,
        facetTypeKey = facetTypeKey,
        scopeKey = scopeKey,
        mergeAction = mergeAction,
        payload = payload,
        createdAt = createdAt,
        createdBy = createdBy,
        lastModifiedAt = lastModifiedAt,
        lastModifiedBy = lastModifiedBy,
        origin = FacetOrigin.CAPTURED,
        originId = repositoryOriginId,
        assignmentUid = uid
    )
