package io.qpointz.mill.metadata.fixtures

import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.repository.FacetRepository
import java.time.Instant
import java.util.UUID

/**
 * Seeds model-level `concept` facet assignments on the model root entity for metadata and AI tests.
 *
 * @see docs/design/agentic/concept-metadata-model.md
 */
object ConceptModelFixtures {

    /** Canonical model root entity id for concept facet assignments. */
    const val MODEL_ENTITY_ID: String = "urn:mill/model/model:model-entity"

    /** Canonical ref for the primary story seed concept. */
    const val VIP_PASSENGERS_REF: String = "urn:mill/model/concept:vip-passengers"

    /** Canonical ref for the premium-customers fixture concept. */
    const val PREMIUM_CUSTOMERS_REF: String = "urn:mill/model/concept:premium-customers"

    /** Canonical ref for the high-value-orders fixture concept. */
    const val HIGH_VALUE_ORDERS_REF: String = "urn:mill/model/concept:high-value-orders"

    /** Default chat write scope used in story fixtures (`w` context). */
    fun defaultWriteScope(): String = MetadataUrns.scopeChat("w")

    /**
     * Persists three concept facet assignments on the model root in [scopeKey].
     *
     * @param repository facet persistence under test
     * @param scopeKey metadata scope URN (defaults to chat scope for `w`)
     * @param actor audit actor id
     * @param instant timestamps for created/updated fields
     */
    fun seed(
        repository: FacetRepository,
        scopeKey: String = defaultWriteScope(),
        actor: String = "fixture",
        instant: Instant = Instant.parse("2026-06-30T12:00:00Z"),
    ) {
        listOf(
            vipPassengersAssignment(scopeKey, actor, instant),
            premiumCustomersAssignment(scopeKey, actor, instant),
            highValueOrdersAssignment(scopeKey, actor, instant),
        ).forEach { repository.save(it) }
    }

    /** Payload for VIP Passengers — primary story acceptance seed. */
    fun vipPassengersPayload(): Map<String, Any?> = mapOf(
        "conceptRef" to VIP_PASSENGERS_REF,
        "concepts" to listOf(
            mapOf(
                "name" to "VIP Passengers",
                "description" to "Passengers traveling in premium cabins or holding elite loyalty status.",
                "sql" to "SELECT p.* FROM skymill.passenger p WHERE p.passenger_class IN ('VIP', 'FIRST') OR p.loyalty_tier >= 3",
                "tags" to listOf("passenger", "premium", "travel"),
            ),
        ),
    )

    /** Payload for Premium Customers fixture concept. */
    fun premiumCustomersPayload(): Map<String, Any?> = mapOf(
        "conceptRef" to PREMIUM_CUSTOMERS_REF,
        "concepts" to listOf(
            mapOf(
                "name" to "Premium Customers",
                "description" to "Customer segment with premium service tier and elevated account balance.",
                "sql" to "segment = 'PREMIUM' AND balance > 100000",
                "tags" to listOf("segmentation", "marketing", "customer"),
            ),
        ),
    )

    /** Payload for High Value Orders fixture concept. */
    fun highValueOrdersPayload(): Map<String, Any?> = mapOf(
        "conceptRef" to HIGH_VALUE_ORDERS_REF,
        "concepts" to listOf(
            mapOf(
                "name" to "High Value Orders",
                "description" to "Orders with total amount above the high-value threshold used for executive reporting.",
                "sql" to "SELECT o.* FROM moneta.orders o WHERE o.total_amount > 5000",
                "tags" to listOf("orders", "revenue", "reporting"),
            ),
        ),
    )

    private fun vipPassengersAssignment(
        scopeKey: String,
        actor: String,
        instant: Instant,
    ): FacetAssignment = conceptAssignment(vipPassengersPayload(), scopeKey, actor, instant)

    private fun premiumCustomersAssignment(
        scopeKey: String,
        actor: String,
        instant: Instant,
    ): FacetAssignment = conceptAssignment(premiumCustomersPayload(), scopeKey, actor, instant)

    private fun highValueOrdersAssignment(
        scopeKey: String,
        actor: String,
        instant: Instant,
    ): FacetAssignment = conceptAssignment(highValueOrdersPayload(), scopeKey, actor, instant)

    private fun conceptAssignment(
        payload: Map<String, Any?>,
        scopeKey: String,
        actor: String,
        instant: Instant,
    ): FacetAssignment =
        FacetAssignment(
            uid = UUID.randomUUID().toString(),
            entityId = MODEL_ENTITY_ID,
            facetTypeKey = MetadataUrns.FACET_TYPE_CONCEPT,
            scopeKey = scopeKey,
            mergeAction = MergeAction.SET,
            payload = payload,
            createdAt = instant,
            createdBy = actor,
            lastModifiedAt = instant,
            lastModifiedBy = actor,
        )
}
