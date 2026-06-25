package io.qpointz.mill.metadata.domain.facet

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FacetProposalMergerTest {

    private val merger = CardinalityAwareFacetProposalMerger()

    @Test
    fun shouldPlanSetRowPerScope_forSingleCardinality() {
        val plans = merger.planAssignments(
            proposal = sampleProposal(listOf("urn:mill/metadata/scope:chat-1")),
            facetTypeCardinality = FacetTargetCardinality.SINGLE,
        )
        assertThat(plans).hasSize(1)
        assertThat(plans[0].mergeAction).isEqualTo(MergeAction.SET)
        assertThat(plans[0].scopeUrn).isEqualTo("urn:mill/metadata/scope:chat-1")
    }

    @Test
    fun shouldPlanSetRowPerScope_forMultipleCardinality() {
        val plans = merger.planAssignments(
            proposal = sampleProposal(
                listOf(
                    "urn:mill/metadata/scope:chat-1",
                    "urn:mill/metadata/scope:chat-2",
                ),
            ),
            facetTypeCardinality = FacetTargetCardinality.MULTIPLE,
        )
        assertThat(plans).hasSize(2)
        assertThat(plans).allMatch { it.mergeAction == MergeAction.SET }
    }

    @Test
    fun shouldReturnEmpty_whenNoWriteScopes() {
        val plans = merger.planAssignments(
            proposal = sampleProposal(emptyList()),
            facetTypeCardinality = FacetTargetCardinality.SINGLE,
        )
        assertThat(plans).isEmpty()
    }

    private fun sampleProposal(scopes: List<String>) = FacetProposalMergerInput(
        metadataEntityId = "urn:mill/model/attribute:skymill/orders/customer_id",
        facetTypeKey = "urn:mill/metadata/facet-type:dq-null-check",
        payload = mapOf("name" to "customer_id_not_null"),
        writeScopeUrns = scopes,
        sourceArtifactId = "artifact-1",
    )
}
