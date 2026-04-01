package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.domain.FacetConverter
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.core.ConceptFacet
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.data.schema.facet.RelationFacet
import io.qpointz.mill.data.schema.facet.StructuralFacet
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import io.qpointz.mill.metadata.service.MetadataContext
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Table

/**
 * Default implementation of [SchemaFacetService].
 *
 * Relational binding uses [MetadataEntityUrnCodec]: entity ids encode physical
 * `schema[.table[.column]]` coordinates. Facet payloads are loaded from [FacetRepository] and
 * merged using the same scope / facet-type candidate rules as the legacy aggregate entity model.
 *
 * @param schemaProvider physical schema source
 * @param metadataEntityRepository persisted entity identities (`metadata_entity`)
 * @param facetRepository facet assignment rows (`metadata_entity_facet`)
 * @param urnCodec decodes entity ids to [CatalogPath] coordinates
 */
class SchemaFacetServiceImpl(
    private val schemaProvider: SchemaProvider,
    private val metadataEntityRepository: MetadataEntityRepository,
    private val facetRepository: FacetRepository,
    private val urnCodec: MetadataEntityUrnCodec = DefaultMetadataEntityUrnCodec()
) : SchemaFacetService {

    private val facetConverter = FacetConverter.defaultConverter()

    /** @see SchemaFacetService.getSchemas */
    override fun getSchemas(context: MetadataContext): SchemaFacetResult {
        val allEntities = metadataEntityRepository.findAll()
        val facetIndex = loadFacetIndex(allEntities)
        val usedEntityIds = mutableSetOf<String>()

        val schemas = schemaProvider.getSchemaNames().map { schemaName ->
            buildSchemaWithFacets(schemaName, allEntities, usedEntityIds, context, facetIndex)
        }

        val unboundMetadata = allEntities.filter { it.id !in usedEntityIds }

        return SchemaFacetResult(schemas = schemas, unboundMetadata = unboundMetadata)
    }

    /** @see SchemaFacetService.getSchema */
    override fun getSchema(schemaName: String, context: MetadataContext): SchemaWithFacets? {
        val schemaNames = schemaProvider.getSchemaNames()
        if (!schemaNames.contains(schemaName)) return null
        val allEntities = metadataEntityRepository.findAll()
        val facetIndex = loadFacetIndex(allEntities)
        val usedEntityIds = mutableSetOf<String>()
        return buildSchemaWithFacets(schemaName, allEntities, usedEntityIds, context, facetIndex)
    }

    /** @see SchemaFacetService.getTable */
    override fun getTable(schemaName: String, tableName: String, context: MetadataContext): SchemaTableWithFacets? {
        val schema = getSchema(schemaName, context) ?: return null
        return schema.tables.firstOrNull { it.tableName == tableName }
    }

    /** @see SchemaFacetService.getColumn */
    override fun getColumn(
        schemaName: String,
        tableName: String,
        columnName: String,
        context: MetadataContext
    ): SchemaColumnWithFacets? {
        val table = getTable(schemaName, tableName, context) ?: return null
        return table.columns.firstOrNull { it.columnName == columnName }
    }

    private fun loadFacetIndex(entities: List<MetadataEntity>): Map<String, List<FacetAssignment>> =
        entities.associate { e -> e.id to facetRepository.findByEntity(e.id) }

    private fun buildSchemaWithFacets(
        schemaName: String,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext,
        facetIndex: Map<String, List<FacetAssignment>>
    ): SchemaWithFacets {
        val physicalSchema = schemaProvider.getSchema(schemaName)
        val schemaEntity = allEntities.find { e ->
            catalogMatches(urnCodec.decode(e.id), schemaName, null, null)
        }
        schemaEntity?.id?.let { usedEntityIds.add(it) }

        val tables = physicalSchema.tablesList.map { table ->
            buildTableWithFacets(schemaName, table, allEntities, usedEntityIds, context, facetIndex)
        }

        return SchemaWithFacets(
            schemaName = schemaName,
            tables = tables,
            metadata = schemaEntity,
            facets = buildFacets(schemaEntity, context, facetIndex)
        )
    }

    private fun buildTableWithFacets(
        schemaName: String,
        table: Table,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext,
        facetIndex: Map<String, List<FacetAssignment>>
    ): SchemaTableWithFacets {
        val tableEntity = allEntities.find { e ->
            catalogMatches(urnCodec.decode(e.id), schemaName, table.name, null)
        }
        tableEntity?.id?.let { usedEntityIds.add(it) }

        val columns = table.fieldsList.map { field ->
            buildColumnWithFacets(schemaName, table.name, field, allEntities, usedEntityIds, context, facetIndex)
        }

        return SchemaTableWithFacets(
            schemaName = schemaName,
            tableName = table.name,
            tableType = table.tableType,
            columns = columns,
            metadata = tableEntity,
            facets = buildFacets(tableEntity, context, facetIndex)
        )
    }

    private fun buildColumnWithFacets(
        schemaName: String,
        tableName: String,
        field: Field,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext,
        facetIndex: Map<String, List<FacetAssignment>>
    ): SchemaColumnWithFacets {
        val columnEntity = allEntities.find { e ->
            catalogMatches(urnCodec.decode(e.id), schemaName, tableName, field.name)
        }
        columnEntity?.id?.let { usedEntityIds.add(it) }

        return SchemaColumnWithFacets(
            schemaName = schemaName,
            tableName = tableName,
            columnName = field.name,
            fieldIndex = field.fieldIdx,
            dataType = field.type,
            metadata = columnEntity,
            facets = buildFacets(columnEntity, context, facetIndex)
        )
    }

    private fun buildFacets(
        entity: MetadataEntity?,
        context: MetadataContext,
        facetIndex: Map<String, List<FacetAssignment>>
    ): SchemaFacets {
        if (entity == null) return SchemaFacets.EMPTY
        val instances = facetIndex[entity.id].orEmpty()
        val facets = mutableSetOf<MetadataFacet>()
        resolveFacetFromInstances(instances, "descriptive", context, DescriptiveFacet::class.java)
            ?.let { facets.add(it) }
        resolveFacetFromInstances(instances, "structural", context, StructuralFacet::class.java)
            ?.let { facets.add(it) }
        resolveFacetFromInstances(instances, "relation", context, RelationFacet::class.java)
            ?.let { facets.add(it) }
        resolveFacetFromInstances(instances, "concept", context, ConceptFacet::class.java)
            ?.let { facets.add(it) }
        resolveFacetFromInstances(instances, "value-mapping", context, ValueMappingFacet::class.java)
            ?.let { facets.add(it) }
        return SchemaFacets(facets)
    }

    /**
     * @param instances facet rows for the entity
     * @param facetType short facet type key (for example `descriptive`)
     * @param context ordered scope context; later scopes override earlier ones when multiple match
     * @param facetClass target facet POJO class
     */
    private fun <T : MetadataFacet> resolveFacetFromInstances(
        instances: List<FacetAssignment>,
        facetType: String,
        context: MetadataContext,
        facetClass: Class<T>
    ): T? {
        var resolved: T? = null
        context.scopes.forEach { scope ->
            resolveFacetTypeCandidates(facetType).forEach { ftCandidate ->
                resolveScopeCandidates(scope).forEach { scopeCandidate ->
                    val match = instances.find { inst ->
                        facetTypeKeyMatches(inst.facetTypeKey, ftCandidate) &&
                            scopeKeyMatches(inst.scopeKey, scopeCandidate)
                    }
                    match?.let { m ->
                        facetConverter.convert(m.payload, facetClass).ifPresent { resolved = it }
                    }
                }
            }
        }
        return resolved
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
     * @param path decoded catalog coordinates from the entity URN
     * @param schemaName physical schema name from the provider
     * @param tableName physical table name, or null when matching schema-level entities
     * @param columnName physical column name, or null when matching schema- or table-level entities
     */
    private fun catalogMatches(
        path: CatalogPath,
        schemaName: String,
        tableName: String?,
        columnName: String?
    ): Boolean {
        if (path.schema == null) return false
        if (!coordinateEquals(path.schema, schemaName)) return false
        if (tableName == null) return path.table == null && path.column == null
        if (!coordinateEquals(path.table, tableName)) return false
        if (columnName == null) return path.column == null
        return coordinateEquals(path.column, columnName)
    }

    private fun resolveScopeCandidates(scope: String): List<String> {
        if (!scope.startsWith(MetadataUrns.SCOPE_PREFIX)) {
            return listOf(scope, MetadataUrns.normaliseScopePath(scope))
        }
        val shortKey = scope.removePrefix(MetadataUrns.SCOPE_PREFIX)
        return listOf(shortKey, scope)
    }

    private fun resolveFacetTypeCandidates(facetType: String): List<String> {
        if (facetType.startsWith(MetadataUrns.FACET_TYPE_PREFIX)) {
            return listOf(facetType)
        }
        return listOf(facetType, MetadataUrns.normaliseFacetTypePath(facetType))
    }

    private fun coordinateEquals(left: String?, right: String?): Boolean {
        if (left == null || right == null) return left == right
        return left.equals(right, ignoreCase = true)
    }
}
