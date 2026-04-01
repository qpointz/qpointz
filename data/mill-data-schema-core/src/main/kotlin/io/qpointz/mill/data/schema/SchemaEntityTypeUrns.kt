package io.qpointz.mill.data.schema

/**
 * Platform vocabulary for facet manifest `applicableTo` entity-type URNs. These strings match
 * `platform-facet-types.json` and normalisation via [io.qpointz.mill.UrnSlug]; they live in the
 * schema boundary so [io.qpointz.mill.metadata.domain.MetadataUrns] stays free of named
 * entity-type taxonomy constants.
 */
object SchemaEntityTypeUrns {
    const val PREFIX = "urn:mill/metadata/entity-type:"
    const val SCHEMA = "urn:mill/metadata/entity-type:schema"
    const val TABLE = "urn:mill/metadata/entity-type:table"
    const val ATTRIBUTE = "urn:mill/metadata/entity-type:attribute"
    const val CONCEPT = "urn:mill/metadata/entity-type:concept"
    /** Logical model root entity (SPEC §3f). */
    const val MODEL = "urn:mill/metadata/entity-type:model"
}
