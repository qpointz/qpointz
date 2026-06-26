package io.qpointz.mill.ai.core.artifact

/**
 * Portable identity for a persisted chat artefact instance.
 *
 * @property id opaque artefact primary key (matches JPA `artifact_id`)
 * @property type canonical type path (`agent/artifact`)
 * @property urn full URN (`urn:agent/artifact:{id}`)
 */
data class ArtifactRef(
    val id: String,
    val type: String = ARTIFACT_TYPE,
    val urn: String,
) {
    companion object {
        /** Canonical artefact type segment in URNs. */
        const val ARTIFACT_TYPE: String = "agent/artifact"

        /**
         * Builds a ref from the opaque artefact id using platform URN rules.
         *
         * @param id persisted artefact id
         */
        fun of(id: String): ArtifactRef = ArtifactRef(
            id = id,
            urn = "urn:$ARTIFACT_TYPE:$id",
        )
    }
}
