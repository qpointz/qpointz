package io.qpointz.mill.data.schema

import io.qpointz.mill.data.metadata.CatalogPath
import io.qpointz.mill.data.metadata.RelationalMetadataEntityUrns
import io.qpointz.mill.metadata.domain.MetadataEntityUrn

/**
 * Schema-layer binding from physical catalog names to **canonical** metadata instance URNs
 * (`urn:mill/model/<kind>:…`) and back.
 *
 * Implementations live in `mill-data-schema-core`; catalog path rules live in `mill-data-metadata`.
 *
 * @see DefaultMetadataEntityUrnCodec
 */
interface MetadataEntityUrnCodec {

    /**
     * @param schema physical schema name from the provider
     * @return canonical entity URN for the schema-level entity
     */
    fun forSchema(schema: String): String

    /**
     * @param schema physical schema name
     * @param table physical table name
     * @return canonical entity URN for the table-level entity
     */
    fun forTable(schema: String, table: String): String

    /**
     * @param schema physical schema name
     * @param table physical table name
     * @param column physical column name
     * @return canonical entity URN for the column-level entity
     */
    fun forAttribute(schema: String, table: String, column: String): String

    /**
     * Parses a canonical metadata entity URN into [CatalogPath] when it denotes a relational
     * instance; otherwise returns a path with all-null coordinates.
     *
     * @param urn entity instance URN (will be canonicalised by the implementation)
     */
    fun decode(urn: String): CatalogPath
}

/**
 * Default [MetadataEntityUrnCodec] using [RelationalMetadataEntityUrns].
 */
class DefaultMetadataEntityUrnCodec : MetadataEntityUrnCodec {

    /** @param schemaName physical schema name as exposed by the provider */
    override fun forSchema(schema: String): String = RelationalMetadataEntityUrns.forSchema(schema)

    /** @param schema physical schema name; @param table physical table name */
    override fun forTable(schema: String, table: String): String =
        RelationalMetadataEntityUrns.forTable(schema, table)

    /** @param schema physical schema name; @param table physical table name; @param column physical column name */
    override fun forAttribute(schema: String, table: String, column: String): String =
        RelationalMetadataEntityUrns.forAttribute(schema, table, column)

    /** @param urn entity instance URN */
    override fun decode(urn: String): CatalogPath = RelationalMetadataEntityUrns.parseCatalogPath(urn)

    /**
     * Returns true if [entityId] denotes the same relational instance as the physical coordinates.
     *
     * @param entityId persisted entity instance URN (may be non-canonical; will be canonicalised)
     * @param schemaName physical schema name from the provider
     * @param tableName physical table name, or null for schema-level match
     * @param columnName physical column name, or null for schema- or table-level match
     */
    fun entityUrnMatchesPhysical(
        entityId: String,
        schemaName: String,
        tableName: String? = null,
        columnName: String? = null,
    ): Boolean {
        val expected = when {
            tableName.isNullOrEmpty() -> forSchema(schemaName)
            columnName.isNullOrEmpty() -> forTable(schemaName, tableName)
            else -> forAttribute(schemaName, tableName, columnName)
        }
        return MetadataEntityUrn.canonicalize(entityId) == MetadataEntityUrn.canonicalize(expected)
    }
}
