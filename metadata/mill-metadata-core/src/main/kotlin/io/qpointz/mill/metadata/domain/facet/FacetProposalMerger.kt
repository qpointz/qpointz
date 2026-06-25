package io.qpointz.mill.metadata.domain.facet

/**
 * Input for planning scope facet assignments from a captured facet proposal (WI-360 handler).
 *
 * @property metadataEntityId target entity URN
 * @property facetTypeKey facet type URN
 * @property payload proposal JSON body
 * @property writeScopeUrns scopes to materialize into
 * @property sourceArtifactId chat artefact id for idempotent retract
 */
data class FacetProposalMergerInput(
    val metadataEntityId: String,
    val facetTypeKey: String,
    val payload: Map<String, Any?>,
    val writeScopeUrns: List<String>,
    val sourceArtifactId: String,
)

/**
 * Planned facet assignment row before persistence.
 *
 * @property entityId target entity URN
 * @property facetTypeKey facet type URN
 * @property scopeUrn scope URN
 * @property mergeAction overlay semantics for this row
 * @property payload facet JSON body
 * @property sourceArtifactId originating chat artefact id
 */
data class FacetAssignmentPlan(
    val entityId: String,
    val facetTypeKey: String,
    val scopeUrn: String,
    val mergeAction: MergeAction,
    val payload: Map<String, Any?>,
    val sourceArtifactId: String,
)

/**
 * Plans how a facet proposal is merged into metadata scopes on {@code artifact.facet.persisted}.
 */
fun interface FacetProposalMerger {

    /**
     * @param proposal captured facet proposal
     * @param facetTypeCardinality catalog cardinality for the facet type
     * @return one plan row per scope in {@link FacetProposalMergerInput#writeScopeUrns}
     */
    fun planAssignments(
        proposal: FacetProposalMergerInput,
        facetTypeCardinality: FacetTargetCardinality,
    ): List<FacetAssignmentPlan>
}

/**
 * Default merger: {@link FacetTargetCardinality#SINGLE} and {@link FacetTargetCardinality#MULTIPLE}
 * both emit one {@link MergeAction#SET} row per scope (read merge applies cardinality).
 */
class CardinalityAwareFacetProposalMerger : FacetProposalMerger {

    override fun planAssignments(
        proposal: FacetProposalMergerInput,
        facetTypeCardinality: FacetTargetCardinality,
    ): List<FacetAssignmentPlan> {
        if (proposal.writeScopeUrns.isEmpty()) {
            return emptyList()
        }
        val mergeAction = when (facetTypeCardinality) {
            FacetTargetCardinality.SINGLE,
            FacetTargetCardinality.MULTIPLE -> MergeAction.SET
        }
        return proposal.writeScopeUrns.map { scopeUrn ->
            FacetAssignmentPlan(
                entityId = proposal.metadataEntityId,
                facetTypeKey = proposal.facetTypeKey,
                scopeUrn = scopeUrn,
                mergeAction = mergeAction,
                payload = proposal.payload,
                sourceArtifactId = proposal.sourceArtifactId,
            )
        }
    }
}
