package io.qpointz.mill.data.schema.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.data.schema.SchemaColumnWithFacets
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.data.schema.api.dto.ColumnDto
import io.qpointz.mill.data.schema.api.dto.DataTypeDescriptor
import io.qpointz.mill.data.schema.api.dto.FacetEnvelopeDto
import io.qpointz.mill.data.schema.api.dto.SchemaContextDto
import io.qpointz.mill.data.schema.api.dto.SchemaDto
import io.qpointz.mill.data.schema.api.dto.SchemaEntityType
import io.qpointz.mill.data.schema.api.dto.SchemaListItemDto
import io.qpointz.mill.data.schema.api.dto.ScopeOptionDto
import io.qpointz.mill.data.schema.api.dto.TableDto
import io.qpointz.mill.data.schema.api.dto.TableSummaryDto
import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.metadata.service.MetadataContext
import io.qpointz.mill.proto.DataType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import io.qpointz.mill.data.backend.SchemaProvider

/**
 * Application service for schema explorer REST API.
 *
 * Accepts raw context query values, parses them using metadata-core rules, and maps
 * schema-core domain objects to REST DTOs.
 */
@Service
class SchemaExplorerService(
    private val schemaFacetService: SchemaFacetService,
    private val schemaProvider: SchemaProvider,
    private val metadataRepository: MetadataRepository,
    private val objectMapper: ObjectMapper
) {
    private enum class FacetMode {
        NONE,
        DIRECT,
        HIERARCHY
    }

    /**
     * Returns the currently selected context for phase 1 UI wiring.
     *
     * @return fixed global context payload
     */
    fun getContext(): SchemaContextDto = SchemaContextDto(
        selectedContext = CONTEXT_GLOBAL,
        availableScopes = listOf(ScopeOptionDto(id = CONTEXT_GLOBAL, slug = CONTEXT_GLOBAL, displayName = "Global"))
    )

    /**
     * Lists all schemas with descriptive facets only.
     *
     * @param context raw context query value
     * @return schema list response
     */
    fun listSchemas(context: String?, facetModeRaw: String?): List<SchemaListItemDto> {
        val metadataContext = parseContext(context)
        val facetMode = parseFacetMode(facetModeRaw)
        log.info("Listing schemas for context scopes={} facetMode={}", metadataContext.scopes, facetMode)
        val schemaNames = schemaProvider.getSchemaNames().toList()
        val entitiesBySchema = metadataRepository.findAll()
            .filter { it.tableName == null && it.attributeName == null && it.schemaName != null }
            .associateBy { it.schemaName!! }
        return schemaNames.map { schemaName ->
            val entity = entitiesBySchema[schemaName]
            SchemaListItemDto(
                id = schemaName,
                entityType = SchemaEntityType.SCHEMA,
                schemaName = schemaName,
                metadataEntityId = entity?.id,
                facets = if (facetMode == FacetMode.NONE) null else mapDescriptiveFacet(entity, metadataContext)
            )
        }
    }

    /**
     * Returns tree payload in one call for model explorer initial load.
     *
     * @param context raw context query value
     * @return schema details with table summaries
     */
    fun getTree(context: String?, facetModeRaw: String?): List<SchemaDto> {
        val metadataContext = parseContext(context)
        val facetMode = parseFacetMode(facetModeRaw)
        log.info("Loading schema tree for context scopes={} facetMode={}", metadataContext.scopes, facetMode)
        return schemaFacetService.getSchemas(metadataContext).schemas.map { schemaToDto(it, facetMode) }
    }

    /**
     * Returns one schema detail by name.
     *
     * @param schemaName schema name
     * @param context raw context query value
     * @return schema detail DTO
     */
    fun getSchema(schemaName: String, context: String?, facetModeRaw: String?): SchemaDto {
        val facetMode = parseFacetMode(facetModeRaw)
        val schema = findSchema(schemaName, parseContext(context))
        return schemaToDto(schema, facetMode)
    }

    /**
     * Returns one table detail by schema/table name.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param context raw context query value
     * @return table detail DTO
     */
    fun getTable(schemaName: String, tableName: String, context: String?, facetModeRaw: String?): TableDto {
        val facetMode = parseFacetMode(facetModeRaw)
        val table = findTable(schemaName, tableName, parseContext(context))
        return tableToDto(table, facetMode)
    }

    /**
     * Returns one column detail by schema/table/column name.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @param context raw context query value
     * @return column detail DTO
     */
    fun getColumn(schemaName: String, tableName: String, columnName: String, context: String?, facetModeRaw: String?): ColumnDto {
        val facetMode = parseFacetMode(facetModeRaw)
        val column = findColumn(schemaName, tableName, columnName, parseContext(context))
        return columnToDto(column, facetMode)
    }

    private fun findSchema(schemaName: String, context: MetadataContext): SchemaWithFacets =
        schemaFacetService.getSchema(schemaName, context)
            ?: throw MillStatuses.notFoundRuntime("Schema not found: $schemaName")

    private fun findTable(schemaName: String, tableName: String, context: MetadataContext): SchemaTableWithFacets =
        schemaFacetService.getTable(schemaName, tableName, context)
            ?: throw MillStatuses.notFoundRuntime("Table not found: $schemaName.$tableName")

    private fun findColumn(
        schemaName: String,
        tableName: String,
        columnName: String,
        context: MetadataContext
    ): SchemaColumnWithFacets =
        schemaFacetService.getColumn(schemaName, tableName, columnName, context)
            ?: throw MillStatuses.notFoundRuntime("Column not found: $schemaName.$tableName.$columnName")

    private fun schemaToDto(schema: SchemaWithFacets, facetMode: FacetMode): SchemaDto = SchemaDto(
        id = schema.schemaName,
        entityType = SchemaEntityType.SCHEMA,
        schemaName = schema.schemaName,
        metadataEntityId = schema.metadata?.id,
        tables = schema.tables.map { table ->
            TableSummaryDto(
                id = "${table.schemaName}.${table.tableName}",
                entityType = SchemaEntityType.TABLE,
                schemaName = table.schemaName,
                tableName = table.tableName,
                metadataEntityId = table.metadata?.id,
                facets = if (facetMode == FacetMode.HIERARCHY) mapFacets(table.facets, descriptiveOnly = true) else null
            )
        },
        facets = if (facetMode != FacetMode.NONE) mapFacets(schema.facets) else null
    )

    private fun tableToDto(table: SchemaTableWithFacets, facetMode: FacetMode): TableDto = TableDto(
        id = "${table.schemaName}.${table.tableName}",
        entityType = SchemaEntityType.TABLE,
        schemaName = table.schemaName,
        tableName = table.tableName,
        tableType = table.tableType.name,
        metadataEntityId = table.metadata?.id,
        columns = table.columns.map { columnToDto(it, if (facetMode == FacetMode.HIERARCHY) FacetMode.HIERARCHY else FacetMode.NONE) },
        facets = if (facetMode != FacetMode.NONE) mapFacets(table.facets) else null
    )

    private fun columnToDto(column: SchemaColumnWithFacets, facetMode: FacetMode): ColumnDto = ColumnDto(
        id = "${column.schemaName}.${column.tableName}.${column.columnName}",
        entityType = SchemaEntityType.COLUMN,
        schemaName = column.schemaName,
        tableName = column.tableName,
        columnName = column.columnName,
        fieldIndex = column.fieldIndex,
        type = toTypeDescriptor(column.dataType),
        metadataEntityId = column.metadata?.id,
        facets = if (facetMode != FacetMode.NONE) mapFacets(column.facets) else null
    )

    private fun toTypeDescriptor(dataType: DataType): DataTypeDescriptor {
        val precision = dataType.type.precision.takeIf { it > 0 }
        val scale = dataType.type.scale.takeIf { it > 0 }
        return DataTypeDescriptor(
            type = dataType.type.typeId.name,
            nullable = dataType.nullability != DataType.Nullability.NOT_NULL,
            precision = precision,
            scale = scale
        )
    }

    private fun mapFacets(
        facets: SchemaFacets,
        descriptiveOnly: Boolean = false
    ): Map<String, FacetEnvelopeDto>? {
        if (facets.isEmpty) return null
        val result = linkedMapOf<String, FacetEnvelopeDto>()
        facets.facetTypes
            .asSequence()
            .filter { !descriptiveOnly || it == "descriptive" }
            .forEach { facetTypeKey ->
                val facet = facets.facetByType<io.qpointz.mill.metadata.domain.MetadataFacet>(facetTypeKey) ?: return@forEach
                val facetTypeUrn = MetadataUrns.normaliseFacetTypePath(facetTypeKey)
                result[facetTypeUrn] = FacetEnvelopeDto(
                    facetType = facetTypeUrn,
                    payload = objectMapper.convertValue(facet, Any::class.java)
                )
            }
        return result.ifEmpty { null }
    }

    private fun mapDescriptiveFacet(entity: MetadataEntity?, context: MetadataContext): Map<String, FacetEnvelopeDto>? {
        if (entity == null) return null
        var payload: Any? = null
        context.scopes.forEach { scope ->
            val candidates = if (scope.startsWith(MetadataUrns.SCOPE_PREFIX)) {
                listOf(scope.removePrefix(MetadataUrns.SCOPE_PREFIX), scope)
            } else {
                listOf(scope)
            }
            candidates.forEach { candidate ->
                entity.getFacet("descriptive", candidate, io.qpointz.mill.metadata.domain.core.DescriptiveFacet::class.java)
                    .ifPresent { payload = objectMapper.convertValue(it, Any::class.java) }
            }
        }
        if (payload == null) return null
        val facetTypeUrn = MetadataUrns.FACET_TYPE_DESCRIPTIVE
        return mapOf(facetTypeUrn to FacetEnvelopeDto(facetType = facetTypeUrn, payload = payload))
    }

    /**
     * Parses raw context input and maps malformed values to BAD_REQUEST status.
     *
     * @param rawContext raw context query parameter
     * @return parsed metadata context
     */
    private fun parseContext(rawContext: String?): MetadataContext = try {
        MetadataContext.parse(rawContext)
    } catch (ex: IllegalArgumentException) {
        throw MillStatuses.badRequestRuntime("Malformed context parameter: ${rawContext ?: "<blank>"}")
    }

    private fun parseFacetMode(rawFacetMode: String?): FacetMode {
        val normalized = rawFacetMode?.trim()?.lowercase()
        return when (normalized) {
            null, "", "direct" -> FacetMode.DIRECT
            "none" -> FacetMode.NONE
            "hierarchy" -> FacetMode.HIERARCHY
            else -> throw MillStatuses.badRequestRuntime("Unsupported facetMode: $rawFacetMode")
        }
    }

    companion object {
        private const val CONTEXT_GLOBAL = "global"
        private val log = LoggerFactory.getLogger(SchemaExplorerService::class.java)
    }
}
