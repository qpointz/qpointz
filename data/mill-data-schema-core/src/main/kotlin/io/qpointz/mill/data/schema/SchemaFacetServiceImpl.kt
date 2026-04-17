package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.metadata.CatalogPath
import io.qpointz.mill.data.metadata.SchemaModelRoot
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.metadata.repository.EntityReadSide
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
 * @param entityRead persisted entity identities (`metadata_entity`)
 * @param facetReadMerge multi-origin read merge (repository + future inferred sources)
 * @param facetCatalog facet type definitions for shaping merged rows into [SchemaFacets]
 * @param urnCodec decodes entity ids to [CatalogPath] coordinates
 */
class SchemaFacetServiceImpl(
    private val schemaProvider: SchemaProvider,
    private val entityRead: EntityReadSide,
    private val facetReadMerge: FacetInstanceReadMerge,
    private val facetCatalog: FacetCatalog,
    private val urnCodec: MetadataEntityUrnCodec = DefaultMetadataEntityUrnCodec()
) : SchemaFacetService {

    /** @see SchemaFacetService.getModelRoot */
    override fun getModelRoot(context: MetadataContext): ModelRootWithFacets {
        val allEntities = entityRead.findAll()
        return buildModelRoot(allEntities, context)
    }

    /** @see SchemaFacetService.getSchemas */
    override fun getSchemas(context: MetadataContext): SchemaFacetResult {
        val allEntities = entityRead.findAll()
        val entityIndex = catalogEntityIndex(allEntities)
        val usedEntityIds = mutableSetOf<String>()
        val modelRoot = buildModelRoot(allEntities, context)
        modelRoot.metadata?.id?.let { usedEntityIds.add(it) }

        val schemas = schemaProvider.getSchemaNames().map { schemaName ->
            buildSchemaWithFacets(schemaName, entityIndex, usedEntityIds, context)
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
            facets = buildFacets(modelEntity, SchemaModelRoot.ENTITY_ID, context)
        )
    }

    /** @see SchemaFacetService.getSchema */
    override fun getSchema(schemaName: String, context: MetadataContext): SchemaWithFacets? {
        if (!schemaProvider.isSchemaExists(schemaName)) {
            return null
        }
        val allEntities = entityRead.findAll()
        val entityIndex = catalogEntityIndex(allEntities)
        val usedEntityIds = mutableSetOf<String>()
        return buildSchemaWithFacets(schemaName, entityIndex, usedEntityIds, context)
    }

    /** @see SchemaFacetService.getTable */
    override fun getTable(schemaName: String, tableName: String, context: MetadataContext): SchemaTableWithFacets? {
        if (!schemaProvider.isSchemaExists(schemaName)) {
            return null
        }
        val millTable = schemaProvider.getTable(schemaName, tableName) ?: return null
        return buildTableWithFacetsNarrow(schemaName, millTable, context)
    }

    /** @see SchemaFacetService.getColumn */
    override fun getColumn(
        schemaName: String,
        tableName: String,
        columnName: String,
        context: MetadataContext
    ): SchemaColumnWithFacets? {
        if (!schemaProvider.isSchemaExists(schemaName)) {
            return null
        }
        val millTable = schemaProvider.getTable(schemaName, tableName) ?: return null
        val field =
            millTable.fieldsList.firstOrNull {
                PhysicalCatalogMatch.coordinateEquals(it.name, columnName)
            }
                ?: return null
        return buildColumnWithFacetsNarrow(schemaName, tableName, field, context)
    }

    private fun buildSchemaWithFacets(
        schemaName: String,
        entityIndex: Map<String, MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext
    ): SchemaWithFacets {
        val physicalSchema = schemaProvider.getSchema(schemaName)
        val schemaEntity = entityIndex[catalogKey(schemaName, null, null)]
        schemaEntity?.id?.let { usedEntityIds.add(it) }

        val tables = physicalSchema.tablesList.map { table ->
            buildTableWithFacets(schemaName, table, entityIndex, usedEntityIds, context)
        }

        return SchemaWithFacets(
            schemaName = schemaName,
            tables = tables,
            metadata = schemaEntity,
            facets = buildFacets(schemaEntity, urnCodec.forSchema(schemaName), context)
        )
    }

    private fun buildTableWithFacets(
        schemaName: String,
        table: Table,
        entityIndex: Map<String, MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext
    ): SchemaTableWithFacets {
        val tableEntity = entityIndex[catalogKey(schemaName, table.name, null)]
        tableEntity?.id?.let { usedEntityIds.add(it) }

        val columns = table.fieldsList.map { field ->
            buildColumnWithFacets(schemaName, table.name, field, entityIndex, usedEntityIds, context)
        }

        return SchemaTableWithFacets(
            schemaName = schemaName,
            tableName = table.name,
            tableType = table.tableType,
            columns = columns,
            metadata = tableEntity,
            facets = buildFacets(tableEntity, urnCodec.forTable(schemaName, table.name), context)
        )
    }

    private fun buildColumnWithFacets(
        schemaName: String,
        tableName: String,
        field: Field,
        entityIndex: Map<String, MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext
    ): SchemaColumnWithFacets {
        val columnEntity = entityIndex[catalogKey(schemaName, tableName, field.name)]
        columnEntity?.id?.let { usedEntityIds.add(it) }

        return SchemaColumnWithFacets(
            schemaName = schemaName,
            tableName = tableName,
            columnName = field.name,
            fieldIndex = field.fieldIdx,
            dataType = field.type,
            metadata = columnEntity,
            facets = buildFacets(
                columnEntity,
                urnCodec.forAttribute(schemaName, tableName, field.name),
                context
            )
        )
    }

    /**
     * Single-table path: one provider lookup, metadata [findById] per node touched, facets only for
     * columns in this table.
     *
     * @param schemaName physical schema name
     * @param table protobuf row with fields already populated
     * @param context read context for facet merge
     */
    private fun buildTableWithFacetsNarrow(
        schemaName: String,
        table: Table,
        context: MetadataContext
    ): SchemaTableWithFacets {
        val tableEntity = entityRead.findById(urnCodec.forTable(schemaName, table.name))
        val columns = table.fieldsList.map { field ->
            buildColumnWithFacetsNarrow(schemaName, table.name, field, context)
        }
        return SchemaTableWithFacets(
            schemaName = schemaName,
            tableName = table.name,
            tableType = table.tableType,
            columns = columns,
            metadata = tableEntity,
            facets = buildFacets(tableEntity, urnCodec.forTable(schemaName, table.name), context)
        )
    }

    /**
     * Single-column path: no full-schema walk and no [findAll] on metadata entities.
     *
     * @param schemaName physical schema name
     * @param tableName physical table name
     * @param field single physical field from the narrow table snapshot
     * @param context read context for facet merge
     */
    private fun buildColumnWithFacetsNarrow(
        schemaName: String,
        tableName: String,
        field: Field,
        context: MetadataContext
    ): SchemaColumnWithFacets {
        val columnEntity = entityRead.findById(
            urnCodec.forAttribute(schemaName, tableName, field.name)
        )
        return SchemaColumnWithFacets(
            schemaName = schemaName,
            tableName = tableName,
            columnName = field.name,
            fieldIndex = field.fieldIdx,
            dataType = field.type,
            metadata = columnEntity,
            facets = buildFacets(
                columnEntity,
                urnCodec.forAttribute(schemaName, tableName, field.name),
                context
            )
        )
    }

    /**
     * @param metadata persisted [metadata_entity] row when matched; may be null for physical-only objects
     * @param mergeEntityIdFallback canonical relational entity URN used when [metadata] is null so
     * inferred sources (e.g. logical layout) still participate in [facetReadMerge]
     * @param context active read scopes and origin allow-list
     */
    private fun buildFacets(
        metadata: MetadataEntity?,
        mergeEntityIdFallback: String,
        context: MetadataContext
    ): SchemaFacets {
        val eid = metadata?.let { MetadataEntityUrn.canonicalize(it.id) }
            ?: MetadataEntityUrn.canonicalize(mergeEntityIdFallback)
        val resolved = facetReadMerge.merge(eid, context)
        return SchemaFacets.fromResolved(resolved, facetCatalog)
    }

    /**
     * Maps relational metadata entities by normalized catalog coordinates for O(1) binding during
     * bulk schema builds.
     *
     * @param entities all persisted entities in the current repository snapshot
     * @return map from [catalogKey] to entity (last duplicate key wins, which should not occur)
     */
    private fun catalogEntityIndex(entities: List<MetadataEntity>): Map<String, MetadataEntity> {
        val map = LinkedHashMap<String, MetadataEntity>()
        for (e in entities) {
            val path = urnCodec.decode(e.id)
            val schema = path.schema ?: continue
            val key = catalogKey(schema, path.table, path.column)
            map[key] = e
        }
        return map
    }

    /**
     * Stable key for catalog coordinates (segments lower-cased; null table/column encoded as empty).
     *
     * @param schemaName physical schema segment
     * @param tableName physical table segment, or null for schema-level entities
     * @param columnName physical column segment, or null for schema- or table-level entities
     */
    private fun catalogKey(schemaName: String, tableName: String?, columnName: String?): String {
        val s = schemaName.lowercase()
        val t = tableName?.lowercase().orEmpty()
        val c = columnName?.lowercase().orEmpty()
        return "$s\u0000$t\u0000$c"
    }

}
