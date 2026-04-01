package io.qpointz.mill.data.schema.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * API entity discriminator for schema explorer payloads.
 */
enum class SchemaEntityType {
    /** Logical catalog model root above physical schemas (SPEC §3f). */
    MODEL,
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
 * One resolved facet row after multi-origin merge (SPEC §3c), aligned with metadata
 * [io.qpointz.mill.metadata.domain.facet.FacetInstance] read model.
 *
 * @property uid stable or synthetic assignment id
 * @property facetTypeUrn facet type URN
 * @property scopeUrn scope URN for this contribution
 * @property origin `CAPTURED` or `INFERRED`
 * @property originId contributing source id
 * @property assignmentUid persisted assignment uid when captured; null for inferred-only rows
 * @property payload facet JSON object
 * @property createdAt creation time
 * @property lastModifiedAt last modification time
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FacetResolvedRowDto(
    @field:Schema(description = "Stable or synthetic facet row id", requiredMode = Schema.RequiredMode.REQUIRED)
    val uid: String,
    @field:Schema(description = "Facet type URN", requiredMode = Schema.RequiredMode.REQUIRED)
    val facetTypeUrn: String,
    @field:Schema(description = "Scope URN", requiredMode = Schema.RequiredMode.REQUIRED)
    val scopeUrn: String,
    @field:Schema(
        description = "CAPTURED (persisted) or INFERRED (read-time)",
        allowableValues = ["CAPTURED", "INFERRED"],
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val origin: String,
    @field:Schema(description = "Contributing metadata source id", requiredMode = Schema.RequiredMode.REQUIRED)
    val originId: String,
    @field:Schema(description = "Persisted assignment uid when origin is CAPTURED")
    val assignmentUid: String?,
    @field:Schema(description = "Facet payload", requiredMode = Schema.RequiredMode.REQUIRED)
    val payload: Map<String, Any?>,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val createdAt: Instant,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val lastModifiedAt: Instant,
)

/**
 * Logical model root returned by [io.qpointz.mill.data.schema.api.SchemaExplorerService.getModelRoot] and tree payloads.
 *
 * @property id path-friendly id ([io.qpointz.mill.data.schema.SchemaModelRoot.ENTITY_LOCAL_ID])
 * @property entityType always [SchemaEntityType.MODEL]
 * @property metadataEntityId canonical metadata entity URN ([io.qpointz.mill.data.schema.SchemaModelRoot.ENTITY_ID])
 * @property facets optional facets map keyed by facet URN
 * @property facetsResolved unified resolved facet rows (captured + inferred) when available
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ModelRootDto(
    val id: String,
    val entityType: SchemaEntityType,
    val metadataEntityId: String,
    val facets: Map<String, FacetEnvelopeDto>? = null,
    val facetsResolved: List<FacetResolvedRowDto>? = null,
)

/**
 * Full explorer tree: model root plus physical schemas (SPEC §3f).
 *
 * @property modelRoot logical model node above all schemas
 * @property schemas physical schemas with table summaries
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SchemaExplorerTreeDto(
    val modelRoot: ModelRootDto,
    val schemas: List<SchemaDto>,
)

/**
 * Schema entry returned by schema list endpoint.
 *
 * @property id stable identifier (`schemaName`, or [io.qpointz.mill.data.schema.SchemaModelRoot.ENTITY_LOCAL_ID] for [SchemaEntityType.MODEL])
 * @property entityType row kind
 * @property schemaName physical schema name; empty for [SchemaEntityType.MODEL]
 * @property metadataEntityId matched metadata entity ID when present; for model, stable URN even when no row exists
 * @property facets optional facets map keyed by facet URN
 * @property facetsResolved optional; omitted on list responses when not loaded (N+1-safe)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SchemaListItemDto(
    val id: String,
    val entityType: SchemaEntityType,
    val schemaName: String,
    val metadataEntityId: String? = null,
    val facets: Map<String, FacetEnvelopeDto>? = null,
    val facetsResolved: List<FacetResolvedRowDto>? = null,
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
 * @property facetsResolved unified resolved facet rows when available
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TableSummaryDto(
    val id: String,
    val entityType: SchemaEntityType,
    val schemaName: String,
    val tableName: String,
    val metadataEntityId: String? = null,
    val facets: Map<String, FacetEnvelopeDto>? = null,
    val facetsResolved: List<FacetResolvedRowDto>? = null,
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
 * @property facetsResolved unified resolved facet rows when available
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SchemaDto(
    val id: String,
    val entityType: SchemaEntityType,
    val schemaName: String,
    val metadataEntityId: String? = null,
    val tables: List<TableSummaryDto>,
    val facets: Map<String, FacetEnvelopeDto>? = null,
    val facetsResolved: List<FacetResolvedRowDto>? = null,
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
 * @property facetsResolved unified resolved facet rows when available
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
    val facetsResolved: List<FacetResolvedRowDto>? = null,
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
 * @property facetsResolved unified resolved facet rows when available
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
    val facetsResolved: List<FacetResolvedRowDto>? = null,
)
