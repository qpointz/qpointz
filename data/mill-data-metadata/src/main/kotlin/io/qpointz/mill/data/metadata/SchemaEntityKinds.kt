package io.qpointz.mill.data.metadata

/**
 * Opaque [io.qpointz.mill.metadata.domain.MetadataEntity.kind] values for relational catalog
 * entities (SPEC §12.3). The data layer sets these when creating or seeding metadata rows;
 * metadata does not infer them from [CatalogPath] or URN structure.
 *
 * Non-relational entities (for example concepts) use caller-defined kinds such as [CONCEPT].
 */
object SchemaEntityKinds {
    /**
     * Logical catalog model root (SPEC §3f). Not a physical schema/table/column; not used in SQL resolution.
     */
    const val MODEL = "model"
    const val SCHEMA = "schema"
    const val TABLE = "table"
    const val ATTRIBUTE = "attribute"
    const val CONCEPT = "concept"
}
