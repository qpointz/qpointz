package io.qpointz.mill.data.schema

/**
 * Opaque [io.qpointz.mill.metadata.domain.MetadataEntity.kind] values for relational catalog
 * entities (SPEC §12.3). The data layer sets these when creating or seeding metadata rows;
 * metadata does not infer them from [CatalogPath] or URN structure.
 *
 * Non-relational entities (for example concepts) use caller-defined kinds such as [CONCEPT].
 */
object SchemaEntityKinds {
    const val SCHEMA = "schema"
    const val TABLE = "table"
    const val ATTRIBUTE = "attribute"
    const val CONCEPT = "concept"
}
