package io.qpointz.mill.ai.capabilities.concept

/**
 * Candidate concept-to-object link emitted outside facet `serializedPayload` at capture time.
 *
 * @property conceptRef stable logical concept id (`urn:mill/model/concept:<slug>`)
 * @property targetRef grounded metadata entity URN for the linked object
 * @property linkKind optional relation intent label (relate pipeline deferred)
 * @property parentFacetArtifactId correlates to the facet-proposal artifact before accept
 */
data class ConceptCandidateLink(
    val conceptRef: String,
    val targetRef: String,
    val linkKind: String? = null,
    val parentFacetArtifactId: String? = null,
) {
    /** Serializes this link for artifact envelope emission. */
    fun toEnvelopeMap(): Map<String, Any?> = buildMap {
        put("conceptRef", conceptRef)
        put("targetRef", targetRef)
        linkKind?.takeIf { it.isNotBlank() }?.let { put("linkKind", it) }
        parentFacetArtifactId?.takeIf { it.isNotBlank() }?.let { put("parentFacetArtifactId", it) }
    }

    companion object {
        /**
         * Parses wire maps from `propose_facet_assignment` tool arguments.
         *
         * @param raw list of link maps from the tool request
         * @return parsed links; invalid rows are dropped
         */
        fun parseAll(raw: List<Map<String, Any?>>?): List<ConceptCandidateLink> =
            raw.orEmpty().mapNotNull { parse(it) }

        private fun parse(raw: Map<String, Any?>): ConceptCandidateLink? {
            val conceptRef = raw["conceptRef"] as? String ?: return null
            val targetRef = raw["targetRef"] as? String ?: return null
            return ConceptCandidateLink(
                conceptRef = conceptRef.trim(),
                targetRef = targetRef.trim(),
                linkKind = raw["linkKind"] as? String,
                parentFacetArtifactId = raw["parentFacetArtifactId"] as? String,
            )
        }
    }
}
