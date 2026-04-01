package io.qpointz.mill.data.schema

/**
 * Stable identity for the catalog-wide logical **model** root (SPEC §3f).
 *
 * The data layer owns this constant; metadata does not special-case it. Facets attach to the
 * metadata entity with [ENTITY_ID] and kind [ENTITY_KIND].
 */
object SchemaModelRoot {

    /** Canonical persisted metadata entity URN for the model root. */
    const val ENTITY_ID: String = "urn:mill/metadata/entity:model-entity"

    /**
     * Short slug suitable for explorer tree node ids and URL path segments (`/model/model-entity`).
     */
    const val ENTITY_LOCAL_ID: String = "model-entity"

    /** Value for [io.qpointz.mill.metadata.domain.MetadataEntity.kind] on the model root row. */
    const val ENTITY_KIND: String = SchemaEntityKinds.MODEL
}
