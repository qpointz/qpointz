package io.qpointz.mill.data.schema.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.qpointz.mill.data.schema.DefaultMetadataEntityUrnCodec
import io.qpointz.mill.data.schema.MetadataEntityUrnCodec
import io.qpointz.mill.data.schema.ModelRootWithFacets
import io.qpointz.mill.data.schema.SchemaColumnWithFacets
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaFacets
import io.qpointz.mill.data.schema.SchemaModelRoot
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.data.schema.api.dto.ColumnDto
import io.qpointz.mill.data.schema.api.dto.DataTypeDescriptor
import io.qpointz.mill.data.schema.api.dto.FacetEnvelopeDto
import io.qpointz.mill.data.schema.api.dto.FacetResolvedRowDto
import io.qpointz.mill.data.schema.api.dto.ModelRootDto
import io.qpointz.mill.data.schema.api.dto.SchemaContextDto
import io.qpointz.mill.data.schema.api.dto.SchemaDto
import io.qpointz.mill.data.schema.api.dto.SchemaEntityType
import io.qpointz.mill.data.schema.api.dto.SchemaExplorerTreeDto
import io.qpointz.mill.data.schema.api.dto.SchemaListItemDto
import io.qpointz.mill.data.schema.api.dto.ScopeOptionDto
import io.qpointz.mill.data.schema.api.dto.TableDto
import io.qpointz.mill.data.schema.api.dto.TableSummaryDto
import io.qpointz.mill.excepions.statuses.MillStatuses
import io.qpointz.mill.metadata.domain.FacetPayloadUtils
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import io.qpointz.mill.metadata.service.MetadataContext
import io.qpointz.mill.proto.DataType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import io.qpointz.mill.data.backend.SchemaProvider

/**
 * Application service for schema explorer REST API.
 *
 * Accepts raw `scope` / legacy `context` query values plus optional `origin`, parses them using
 * [metadata-core][io.qpointz.mill.metadata.service.MetadataReadContext] rules, and maps schema-core domain
 * objects to REST DTOs.
 */
@Service
class SchemaExplorerService(
    private val schemaFacetService: SchemaFacetService,
    private val schemaProvider: SchemaProvider,
    private val metadataEntityRepository: MetadataEntityRepository,
    private val facetRepository: FacetRepository,
    private val objectMapper: ObjectMapper,
    private val urnCodec: MetadataEntityUrnCodec = DefaultMetadataEntityUrnCodec()
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
     * @param scope preferred comma-separated scope query value
     * @param contextLegacy deprecated alias for [scope] when [scope] is absent
     * @param origin optional comma-separated origin ids
     * @return schema list response
     */
    fun listSchemas(scope: String?, contextLegacy: String?, origin: String?, facetModeRaw: String?): List<SchemaListItemDto> {
        val metadataContext = parseReadContext(scope, contextLegacy, origin)
        val facetMode = parseFacetMode(facetModeRaw)
        log.info("Listing schemas for scopes={} facetMode={}", metadataContext.scopes, facetMode)
        val schemaNames = schemaProvider.getSchemaNames().toList()
        val allEntities = metadataEntityRepository.findAll()
        val modelEntity = findModelMetadataEntity(allEntities)
        val modelListItem = SchemaListItemDto(
            id = SchemaModelRoot.ENTITY_LOCAL_ID,
            entityType = SchemaEntityType.MODEL,
            schemaName = "",
            metadataEntityId = modelMetadataEntityIdForApi(modelEntity),
            facets = if (facetMode == FacetMode.NONE) null else mapDescriptiveFacet(modelEntity, metadataContext)
        )
        val entitiesBySchema = allEntities
            .mapNotNull { e ->
                val p = urnCodec.decode(e.id)
                if (p.table != null || p.column != null) return@mapNotNull null
                val schema = p.schema ?: return@mapNotNull null
                schema to e
            }
            .associateBy({ it.first.lowercase() }, { it.second })
        val schemaItems = schemaNames.map { schemaName ->
            val entity = entitiesBySchema[schemaName.lowercase()]
            SchemaListItemDto(
                id = schemaName,
                entityType = SchemaEntityType.SCHEMA,
                schemaName = schemaName,
                metadataEntityId = entity?.id,
                facets = if (facetMode == FacetMode.NONE) null else mapDescriptiveFacet(entity, metadataContext)
            )
        }
        return listOf(modelListItem) + schemaItems
    }

    /**
     * @param entities all persisted metadata entities from the repository
     * @return entity row for the model root when present
     */
    private fun findModelMetadataEntity(entities: List<MetadataEntity>): MetadataEntity? {
        val canonical = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID)
        return entities.find { MetadataEntityUrn.canonicalize(it.id) == canonical }
    }

    /**
     * @param modelEntity persisted model row, or null when the catalog has no model entity yet
     * @return canonical URN clients use for metadata facet APIs ([SchemaModelRoot.ENTITY_ID])
     */
    private fun modelMetadataEntityIdForApi(modelEntity: MetadataEntity?): String =
        modelEntity?.let { MetadataEntityUrn.canonicalize(it.id) }
            ?: MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID)

    /**
     * Returns tree payload in one call for model explorer initial load.
     *
     * @param scope preferred comma-separated scope query value
     * @param contextLegacy deprecated alias for [scope] when [scope] is absent
     * @param origin optional comma-separated origin ids
     * @return schema details with table summaries
     */
    fun getTree(scope: String?, contextLegacy: String?, origin: String?, facetModeRaw: String?): SchemaExplorerTreeDto {
        val metadataContext = parseReadContext(scope, contextLegacy, origin)
        val facetMode = parseFacetMode(facetModeRaw)
        log.info("Loading schema tree for scopes={} facetMode={}", metadataContext.scopes, facetMode)
        val result = schemaFacetService.getSchemas(metadataContext)
        return SchemaExplorerTreeDto(
            modelRoot = modelRootToDto(result.modelRoot, facetMode),
            schemas = result.schemas.map { schemaToDto(it, facetMode) }
        )
    }

    /**
     * Returns the logical model root with optional facet expansion.
     *
     * @param scope preferred comma-separated scope query value
     * @param contextLegacy deprecated alias for [scope] when [scope] is absent
     * @param origin optional comma-separated origin ids
     * @param facetModeRaw facet expansion policy (`none` \| `direct` \| `hierarchy`)
     * @return model root DTO with stable [ModelRootDto.metadataEntityId]
     */
    fun getModelRoot(scope: String?, contextLegacy: String?, origin: String?, facetModeRaw: String?): ModelRootDto {
        val metadataContext = parseReadContext(scope, contextLegacy, origin)
        val facetMode = parseFacetMode(facetModeRaw)
        return modelRootToDto(schemaFacetService.getModelRoot(metadataContext), facetMode)
    }

    /**
     * @param root domain model root from [SchemaFacetService]
     * @param facetMode facet payload expansion for the DTO
     */
    private fun modelRootToDto(root: ModelRootWithFacets, facetMode: FacetMode): ModelRootDto = ModelRootDto(
        id = SchemaModelRoot.ENTITY_LOCAL_ID,
        entityType = SchemaEntityType.MODEL,
        metadataEntityId = root.metadataEntityId,
        facets = if (facetMode == FacetMode.NONE) null else mapFacets(root.facets),
        facetsResolved = mapFacetsResolved(root.facets)
    )

    /**
     * Returns one schema detail by name.
     *
     * @param schemaName schema name
     * @param scope preferred comma-separated scope query value
     * @param contextLegacy deprecated alias for [scope] when [scope] is absent
     * @param origin optional comma-separated origin ids
     * @return schema detail DTO
     */
    fun getSchema(schemaName: String, scope: String?, contextLegacy: String?, origin: String?, facetModeRaw: String?): SchemaDto {
        val facetMode = parseFacetMode(facetModeRaw)
        val schema = findSchema(schemaName, parseReadContext(scope, contextLegacy, origin))
        return schemaToDto(schema, facetMode)
    }

    /**
     * Returns one table detail by schema/table name.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param scope preferred comma-separated scope query value
     * @param contextLegacy deprecated alias for [scope] when [scope] is absent
     * @param origin optional comma-separated origin ids
     * @return table detail DTO
     */
    fun getTable(
        schemaName: String,
        tableName: String,
        scope: String?,
        contextLegacy: String?,
        origin: String?,
        facetModeRaw: String?
    ): TableDto {
        val facetMode = parseFacetMode(facetModeRaw)
        val table = findTable(schemaName, tableName, parseReadContext(scope, contextLegacy, origin))
        return tableToDto(table, facetMode)
    }

    /**
     * Returns one column detail by schema/table/column name.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @param scope preferred comma-separated scope query value
     * @param contextLegacy deprecated alias for [scope] when [scope] is absent
     * @param origin optional comma-separated origin ids
     * @return column detail DTO
     */
    fun getColumn(
        schemaName: String,
        tableName: String,
        columnName: String,
        scope: String?,
        contextLegacy: String?,
        origin: String?,
        facetModeRaw: String?
    ): ColumnDto {
        val facetMode = parseFacetMode(facetModeRaw)
        val column = findColumn(schemaName, tableName, columnName, parseReadContext(scope, contextLegacy, origin))
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
                facets = if (facetMode == FacetMode.HIERARCHY) mapFacets(table.facets, descriptiveOnly = true) else null,
                facetsResolved = mapFacetsResolved(table.facets)
            )
        },
        facets = if (facetMode != FacetMode.NONE) mapFacets(schema.facets) else null,
        facetsResolved = mapFacetsResolved(schema.facets)
    )

    private fun tableToDto(table: SchemaTableWithFacets, facetMode: FacetMode): TableDto = TableDto(
        id = "${table.schemaName}.${table.tableName}",
        entityType = SchemaEntityType.TABLE,
        schemaName = table.schemaName,
        tableName = table.tableName,
        tableType = table.tableType.name,
        metadataEntityId = table.metadata?.id,
        columns = table.columns.map { columnToDto(it, if (facetMode == FacetMode.HIERARCHY) FacetMode.HIERARCHY else FacetMode.NONE) },
        facets = if (facetMode != FacetMode.NONE) mapFacets(table.facets) else null,
        facetsResolved = mapFacetsResolved(table.facets)
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
        facets = if (facetMode != FacetMode.NONE) mapFacets(column.facets) else null,
        facetsResolved = mapFacetsResolved(column.facets)
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

    /**
     * Maps merged [SchemaFacets.facetsResolved] rows to API DTOs.
     *
     * @param facets schema facet bundle from the schema facet service
     * @return null when there are no resolved rows (omitted from JSON)
     */
    private fun mapFacetsResolved(facets: SchemaFacets): List<FacetResolvedRowDto>? {
        if (facets.facetsResolved.isEmpty()) return null
        return facets.facetsResolved.map { fi ->
            FacetResolvedRowDto(
                uid = fi.uid,
                facetTypeUrn = MetadataEntityUrn.canonicalize(fi.facetTypeKey),
                scopeUrn = MetadataEntityUrn.canonicalize(fi.scopeKey),
                origin = fi.origin.name,
                originId = fi.originId,
                assignmentUid = fi.assignmentUid,
                payload = fi.payload,
                createdAt = fi.createdAt,
                lastModifiedAt = fi.lastModifiedAt
            )
        }
    }

    private fun mapDescriptiveFacet(entity: MetadataEntity?, context: MetadataContext): Map<String, FacetEnvelopeDto>? {
        if (entity == null) return null
        val instances = facetRepository.findByEntity(entity.id)
        var resolved: DescriptiveFacet? = null
        context.scopes.forEach { scope ->
            facetTypeCandidates("descriptive").forEach { ft ->
                scopeCandidates(scope).forEach { sc ->
                    val match = instances.find { inst ->
                        facetTypeKeyMatches(inst.facetTypeKey, ft) && scopeKeyMatches(inst.scopeKey, sc)
                    }
                    match?.let { m ->
                        FacetPayloadUtils.convert(m.payload, DescriptiveFacet::class.java)
                            .ifPresent { resolved = it }
                    }
                }
            }
        }
        val payload = resolved?.let { objectMapper.convertValue(it, Any::class.java) } ?: return null
        val facetTypeUrn = MetadataUrns.FACET_TYPE_DESCRIPTIVE
        return mapOf(facetTypeUrn to FacetEnvelopeDto(facetType = facetTypeUrn, payload = payload))
    }

    private fun facetTypeCandidates(facetType: String): List<String> {
        if (facetType.startsWith(MetadataUrns.FACET_TYPE_PREFIX)) return listOf(facetType)
        return listOf(facetType, MetadataUrns.normaliseFacetTypePath(facetType))
    }

    private fun scopeCandidates(scope: String): List<String> {
        if (!scope.startsWith(MetadataUrns.SCOPE_PREFIX)) {
            return listOf(scope, MetadataUrns.normaliseScopePath(scope))
        }
        val shortKey = scope.removePrefix(MetadataUrns.SCOPE_PREFIX)
        return listOf(shortKey, scope)
    }

    private fun facetTypeKeyMatches(stored: String, candidate: String): Boolean {
        val s = runCatching { MetadataEntityUrn.canonicalize(stored) }.getOrNull() ?: return false
        val c = runCatching { MetadataEntityUrn.canonicalize(MetadataUrns.normaliseFacetTypePath(candidate)) }
            .getOrNull() ?: return false
        return s == c
    }

    private fun scopeKeyMatches(stored: String, candidate: String): Boolean {
        val s = runCatching { MetadataEntityUrn.canonicalize(stored) }.getOrNull() ?: return false
        val c = runCatching { MetadataEntityUrn.canonicalize(MetadataUrns.normaliseScopePath(candidate)) }
            .getOrNull() ?: return false
        return s == c
    }

    /**
     * Parses read query parameters: effective scope is [scope] when non-blank, else [contextLegacy] (migration).
     *
     * @param scope preferred comma-separated scope segments
     * @param contextLegacy deprecated alias when [scope] is null or blank
     * @param origin optional comma-separated origin ids
     * @return parsed metadata read context
     */
    private fun parseReadContext(scope: String?, contextLegacy: String?, origin: String?): MetadataContext {
        val effectiveScope = scope?.takeIf { it.isNotBlank() } ?: contextLegacy
        return try {
            MetadataContext.parse(effectiveScope, origin)
        } catch (ex: IllegalArgumentException) {
            throw MillStatuses.badRequestRuntime("Malformed scope parameter: ${effectiveScope ?: "<blank>"}")
        }
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
