package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.domain.FacetConverter
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.FacetInstanceReadMerge
import io.qpointz.mill.metadata.service.MetadataContext
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Table

/**
 * Default implementation of [SchemaFacetService].
 *
 * Relational binding uses [MetadataEntityUrnCodec]: entity ids encode physical
 * `schema[.table[.column]]` coordinates. Effective facets are loaded via [FacetInstanceReadMerge]
 * over all registered [io.qpointz.mill.metadata.source.MetadataSource] beans (SPEC §3i).
 *
 * @param schemaProvider physical schema source
 * @param metadataEntityRepository persisted entity identities (`metadata_entity`)
 * @param facetReadMerge multi-origin read merge (repository + future inferred sources)
 * @param facetCatalog facet type definitions for shaping merged rows into [SchemaFacets]
 * @param urnCodec decodes entity ids to [CatalogPath] coordinates
 */
class SchemaFacetServiceImpl(
    private val schemaProvider: SchemaProvider,
    private val metadataEntityRepository: MetadataEntityRepository,
    private val facetReadMerge: FacetInstanceReadMerge,
    private val facetCatalog: FacetCatalog,
    private val urnCodec: MetadataEntityUrnCodec = DefaultMetadataEntityUrnCodec()
) : SchemaFacetService {

    private val facetConverter = FacetConverter.defaultConverter()

    /** @see SchemaFacetService.getModelRoot */
    override fun getModelRoot(context: MetadataContext): ModelRootWithFacets {
        val allEntities = metadataEntityRepository.findAll()
        return buildModelRoot(allEntities, context)
    }

    /** @see SchemaFacetService.getSchemas */
    override fun getSchemas(context: MetadataContext): SchemaFacetResult {
        val allEntities = metadataEntityRepository.findAll()
        val usedEntityIds = mutableSetOf<String>()
        val modelRoot = buildModelRoot(allEntities, context)
        modelRoot.metadata?.id?.let { usedEntityIds.add(it) }

        val schemas = schemaProvider.getSchemaNames().map { schemaName ->
            buildSchemaWithFacets(schemaName, allEntities, usedEntityIds, context)
        }

        val unboundMetadata = allEntities.filter { it.id !in usedEntityIds }

        return SchemaFacetResult(modelRoot = modelRoot, schemas = schemas, unboundMetadata = unboundMetadata)
    }

    /**
     * Resolves the stable model root entity and its facets.
     *
     * @param allEntities all persisted metadata entities
     * @param context scope stack for facet resolution
     */
    private fun buildModelRoot(
        allEntities: List<MetadataEntity>,
        context: MetadataContext
    ): ModelRootWithFacets {
        val canonicalModelId = MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID)
        val modelEntity = allEntities.find { MetadataEntityUrn.canonicalize(it.id) == canonicalModelId }
        return ModelRootWithFacets(
            metadataEntityId = canonicalModelId,
            metadata = modelEntity,
            facets = buildFacets(modelEntity, context)
        )
    }

    /** @see SchemaFacetService.getSchema */
    override fun getSchema(schemaName: String, context: MetadataContext): SchemaWithFacets? {
        val schemaNames = schemaProvider.getSchemaNames()
        if (!schemaNames.contains(schemaName)) return null
        val allEntities = metadataEntityRepository.findAll()
        val usedEntityIds = mutableSetOf<String>()
        return buildSchemaWithFacets(schemaName, allEntities, usedEntityIds, context)
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

    private fun buildSchemaWithFacets(
        schemaName: String,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext
    ): SchemaWithFacets {
        val physicalSchema = schemaProvider.getSchema(schemaName)
        val schemaEntity = allEntities.find { e ->
            catalogMatches(urnCodec.decode(e.id), schemaName, null, null)
        }
        schemaEntity?.id?.let { usedEntityIds.add(it) }

        val tables = physicalSchema.tablesList.map { table ->
            buildTableWithFacets(schemaName, table, allEntities, usedEntityIds, context)
        }

        return SchemaWithFacets(
            schemaName = schemaName,
            tables = tables,
            metadata = schemaEntity,
            facets = buildFacets(schemaEntity, context)
        )
    }

    private fun buildTableWithFacets(
        schemaName: String,
        table: Table,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext
    ): SchemaTableWithFacets {
        val tableEntity = allEntities.find { e ->
            catalogMatches(urnCodec.decode(e.id), schemaName, table.name, null)
        }
        tableEntity?.id?.let { usedEntityIds.add(it) }

        val columns = table.fieldsList.map { field ->
            buildColumnWithFacets(schemaName, table.name, field, allEntities, usedEntityIds, context)
        }

        return SchemaTableWithFacets(
            schemaName = schemaName,
            tableName = table.name,
            tableType = table.tableType,
            columns = columns,
            metadata = tableEntity,
            facets = buildFacets(tableEntity, context)
        )
    }

    private fun buildColumnWithFacets(
        schemaName: String,
        tableName: String,
        field: Field,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext
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
            facets = buildFacets(columnEntity, context)
        )
    }

    private fun buildFacets(
        entity: MetadataEntity?,
        context: MetadataContext
    ): SchemaFacets {
        if (entity == null) return SchemaFacets.EMPTY
        val eid = MetadataEntityUrn.canonicalize(entity.id)
        val resolved = facetReadMerge.merge(eid, context)
        return SchemaFacets.fromResolved(resolved, facetConverter, facetCatalog)
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

    private fun coordinateEquals(left: String?, right: String?): Boolean {
        if (left == null || right == null) return left == right
        return left.equals(right, ignoreCase = true)
    }
}
