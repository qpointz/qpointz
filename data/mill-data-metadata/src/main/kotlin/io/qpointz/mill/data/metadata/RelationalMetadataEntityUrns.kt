package io.qpointz.mill.data.metadata

/**
 * Builds and parses typed model entity URNs for relational catalog objects.
 *
 * Delegates entirely to [ModelEntityUrn] — this object is kept as a named alias so existing
 * call sites in `mill-data-schema-core` and `mill-data-metadata` can migrate incrementally.
 *
 * @see ModelEntityUrn
 */
object RelationalMetadataEntityUrns {

    /**
     * @param schema physical schema name from the provider
     * @return canonical entity URN for the schema-level entity
     */
    fun forSchema(schema: String): String = ModelEntityUrn.forSchema(schema)

    /**
     * @param schema physical schema name
     * @param table physical table name
     * @return canonical entity URN for the table-level entity
     */
    fun forTable(schema: String, table: String): String = ModelEntityUrn.forTable(schema, table)

    /**
     * @param schema physical schema name
     * @param table physical table name
     * @param column physical column name (dots within the name are preserved)
     * @return canonical entity URN for the column-level entity
     */
    fun forAttribute(schema: String, table: String, column: String): String =
        ModelEntityUrn.forAttribute(schema, table, column)

    /**
     * Parses a typed model entity URN into [CatalogPath].
     * Non-relational and unrecognised URNs yield all-null coordinates.
     *
     * @param urn entity instance URN
     */
    fun parseCatalogPath(urn: String): CatalogPath = ModelEntityUrn.parseCatalogPath(urn)
}
