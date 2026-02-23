package io.qpointz.mill.metadata.domain

/** Common lifecycle contract for all metadata facets. */
interface MetadataFacet {
    /** Stable type key used in persisted facet maps (for example `descriptive`). */
    val facetType: String
    /** Attaches the owning entity instance to the facet. */
    fun setOwner(owner: MetadataEntity)
    /** Validates semantic integrity of the facet content. */
    fun validate()
    /** Merges another facet instance into this facet. */
    fun merge(other: MetadataFacet): MetadataFacet
}

/** Base implementation used by most concrete facet types. */
abstract class AbstractFacet : MetadataFacet {
    var owner: MetadataEntity? = null
        private set

    override fun setOwner(owner: MetadataEntity) {
        this.owner = owner
    }

    override fun validate() {}
    override fun merge(other: MetadataFacet): MetadataFacet = this
}
