package io.qpointz.mill.data.schema.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

/**
 * API entity discriminator for schema explorer payloads.
 */
enum class SchemaEntityType {
    SCHEMA,
    TABLE,
    COLUMN,
}

/**
 * Serialized data type descriptor for schema columns.
 *
 * @property type canonical Mill type identifier
 * @property nullable whether null values are allowed
 * @property precision optional numeric precision when available
 * @property scale optional numeric scale when available
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DataTypeDescriptor(
    @field:Schema(description = "Canonical Mill type identifier, e.g. STRING, BIG_INT, DECIMAL")
    val type: String,
    @field:Schema(description = "Whether the column type allows null values")
    val nullable: Boolean,
    @field:Schema(description = "Optional precision for decimal/timestamp-like types")
    val precision: Int? = null,
    @field:Schema(description = "Optional scale for decimal-like types")
    val scale: Int? = null,
)

/**
 * Facet envelope with explicit type metadata and raw payload.
 *
 * @property facetType facet type URN
 * @property payload facet payload object
 */
data class FacetEnvelopeDto(
    @field:Schema(description = "Facet type URN, e.g. urn:mill/metadata/facet-type:descriptive")
    val facetType: String,
    @field:Schema(description = "Facet payload content")
    val payload: Any,
)

/**
 * Schema entry returned by schema list endpoint.
 *
 * @property id stable identifier (`schemaName`)
 * @property entityType discriminator fixed to [SchemaEntityType.SCHEMA]
 * @property schemaName physical schema name
 * @property metadataEntityId matched metadata entity ID when present
 * @property facets optional facets map keyed by facet URN
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SchemaListItemDto(
    val id: String,
    val entityType: SchemaEntityType,
    val schemaName: String,
    val metadataEntityId: String? = null,
    val facets: Map<String, FacetEnvelopeDto>? = null,
)

/**
 * Table summary embedded in schema detail responses.
 *
 * @property id stable identifier (`schema.table`)
 * @property entityType discriminator fixed to [SchemaEntityType.TABLE]
 * @property schemaName parent schema
 * @property tableName table name
 * @property metadataEntityId matched metadata entity ID when present
 * @property facets optional facets map keyed by facet URN
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TableSummaryDto(
    val id: String,
    val entityType: SchemaEntityType,
    val schemaName: String,
    val tableName: String,
    val metadataEntityId: String? = null,
    val facets: Map<String, FacetEnvelopeDto>? = null,
)

/**
 * Schema detail payload.
 *
 * @property id stable identifier (`schemaName`)
 * @property entityType discriminator fixed to [SchemaEntityType.SCHEMA]
 * @property schemaName physical schema name
 * @property metadataEntityId matched metadata entity ID when present
 * @property tables table summaries in this schema
 * @property facets optional facets map keyed by facet URN
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SchemaDto(
    val id: String,
    val entityType: SchemaEntityType,
    val schemaName: String,
    val metadataEntityId: String? = null,
    val tables: List<TableSummaryDto>,
    val facets: Map<String, FacetEnvelopeDto>? = null,
)

/**
 * Column payload.
 *
 * @property id stable identifier (`schema.table.column`)
 * @property entityType discriminator fixed to [SchemaEntityType.COLUMN]
 * @property schemaName parent schema
 * @property tableName parent table
 * @property columnName physical column name
 * @property fieldIndex column ordinal in the physical schema
 * @property type column type descriptor
 * @property metadataEntityId matched metadata entity ID when present
 * @property facets optional facets map keyed by facet URN
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ColumnDto(
    val id: String,
    val entityType: SchemaEntityType,
    val schemaName: String,
    val tableName: String,
    val columnName: String,
    val fieldIndex: Int,
    val type: DataTypeDescriptor,
    val metadataEntityId: String? = null,
    val facets: Map<String, FacetEnvelopeDto>? = null,
)

/**
 * Table detail payload.
 *
 * @property id stable identifier (`schema.table`)
 * @property entityType discriminator fixed to [SchemaEntityType.TABLE]
 * @property schemaName parent schema
 * @property tableName table name
 * @property tableType physical table type
 * @property metadataEntityId matched metadata entity ID when present
 * @property columns columns in this table
 * @property facets optional facets map keyed by facet URN
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TableDto(
    val id: String,
    val entityType: SchemaEntityType,
    val schemaName: String,
    val tableName: String,
    val tableType: String,
    val metadataEntityId: String? = null,
    val columns: List<ColumnDto>,
    val facets: Map<String, FacetEnvelopeDto>? = null,
)
