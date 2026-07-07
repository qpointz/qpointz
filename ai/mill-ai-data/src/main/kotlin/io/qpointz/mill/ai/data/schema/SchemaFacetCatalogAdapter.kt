package io.qpointz.mill.ai.data.schema

import io.qpointz.mill.ai.capabilities.metadata.MetadataEntityIds
import io.qpointz.mill.ai.capabilities.schema.ListColumnsItem
import io.qpointz.mill.ai.capabilities.schema.ListRelationsItem
import io.qpointz.mill.ai.capabilities.schema.ListSchemasItem
import io.qpointz.mill.ai.capabilities.schema.ListTablesItem
import io.qpointz.mill.ai.capabilities.schema.RelationDirection
import io.qpointz.mill.ai.capabilities.schema.SchemaCatalogPort
import io.qpointz.mill.data.schema.PhysicalCatalogMatch
import io.qpointz.mill.data.schema.SchemaFacetResult
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.SchemaWithFacets
import io.qpointz.mill.data.schema.WithFacets
import io.qpointz.mill.data.schema.facet.RelationFacet
import io.qpointz.mill.metadata.domain.RelationCardinality
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * [SchemaCatalogPort] backed by Mill Data [SchemaFacetService].
 *
 * Caches the full [SchemaFacetService.getSchemas] snapshot so repeated `list_schemas` /
 * `list_tables` / `list_columns` / `list_relations` calls in one agent turn do not re-walk the
 * physical catalog and facet merge. Cold `list_tables` / `list_columns` use narrow service paths
 * when no snapshot is warm yet.
 *
 * Facet capture can change descriptions/relations within a chat session — call [invalidateCache]
 * after proposal persist/retract (and rely on [ttlMillis] as a safety net for other writers).
 *
 * @param svc Mill Data schema + facet aggregate
 * @param ttlMillis maximum age of a cached snapshot before automatic reload (default 30s)
 */
class SchemaFacetCatalogAdapter(
    private val svc: SchemaFacetService,
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
) : SchemaCatalogPort {

    private val lock = ReentrantReadWriteLock()
    private var cachedSnapshot: SchemaFacetResult? = null
    private var cachedAtMillis: Long = 0L

    private fun description(item: WithFacets): String =
        item.facets.descriptive?.description ?: ""

    private fun displayName(item: WithFacets): String =
        item.facets.descriptive?.displayName ?: ""

    override fun invalidateCache() {
        lock.write {
            cachedSnapshot = null
            cachedAtMillis = 0L
        }
    }

    override fun listSchemas(): List<ListSchemasItem> =
        snapshot().schemas.map { schema ->
            ListSchemasItem(
                schemaName = schema.schemaName,
                description = description(schema),
                displayName = displayName(schema),
                catalogPath = schema.schemaName,
                metadataEntityUrn = MetadataEntityIds.resolve(schema.schemaName),
            )
        }

    override fun listTables(schemaName: String): List<ListTablesItem> {
        peekSnapshot()?.let { return mapTables(it, schemaName) }
        val schema = svc.getSchema(schemaName) ?: return emptyList()
        return mapTables(schema)
    }

    override fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem> {
        peekSnapshot()?.let { return mapColumns(it, schemaName, tableName) }
        val table = svc.getTable(schemaName, tableName) ?: return emptyList()
        return mapColumns(table)
    }

    override fun listRelations(
        schemaName: String,
        tableName: String,
        direction: RelationDirection,
    ): List<ListRelationsItem> {
        fun createRelation(relation: RelationFacet.Relation): ListRelationsItem? {
            val source = relation.sourceTable ?: return null
            val target = relation.targetTable ?: return null
            return ListRelationsItem(
                source.schema,
                source.table,
                relation.sourceAttributes,
                target.schema,
                target.table,
                relation.targetAttributes,
                relation.name ?: "",
                relation.description ?: "",
                relation.cardinality ?: RelationCardinality.MANY_TO_ONE,
                relation.joinSql ?: "",
            )
        }

        val includeOutbound = direction == RelationDirection.OUTBOUND || direction == RelationDirection.BOTH
        val includeInbound = direction == RelationDirection.INBOUND || direction == RelationDirection.BOTH

        val facetResult = snapshot()
        val fromModel = facetResult.modelRoot.facets.relation?.relations.orEmpty()
        val fromTables =
            facetResult.schemas
                .asSequence()
                .flatMap { it.tables }
                .mapNotNull { it.facets.relation?.relations }
                .flatten()
                .toList()
        val allRelations = fromModel + fromTables

        return allRelations
            .asSequence()
            .mapNotNull { relation ->
                when {
                    includeOutbound &&
                        PhysicalCatalogMatch.coordinateEquals(relation.sourceTable?.schema, schemaName) &&
                        PhysicalCatalogMatch.coordinateEquals(relation.sourceTable?.table, tableName) ->
                        createRelation(relation)
                    includeInbound &&
                        PhysicalCatalogMatch.coordinateEquals(relation.targetTable?.schema, schemaName) &&
                        PhysicalCatalogMatch.coordinateEquals(relation.targetTable?.table, tableName) ->
                        createRelation(relation)
                    else -> null
                }
            }
            .toList()
    }

    private fun mapTables(snapshot: SchemaFacetResult, schemaName: String): List<ListTablesItem> =
        snapshot.schemas
            .filter { PhysicalCatalogMatch.coordinateEquals(it.schemaName, schemaName) }
            .flatMap { mapTables(it) }

    private fun mapTables(schema: SchemaWithFacets): List<ListTablesItem> =
        schema.tables.map { table ->
            val catalogPath = "${table.schemaName}.${table.tableName}"
            ListTablesItem(
                schemaName = table.schemaName,
                tableName = table.tableName,
                description = description(table),
                displayName = displayName(table),
                catalogPath = catalogPath,
                metadataEntityUrn = MetadataEntityIds.resolve(catalogPath),
            )
        }

    private fun mapColumns(
        snapshot: SchemaFacetResult,
        schemaName: String,
        tableName: String,
    ): List<ListColumnsItem> =
        snapshot.schemas
            .filter { PhysicalCatalogMatch.coordinateEquals(it.schemaName, schemaName) }
            .flatMap { it.tables }
            .filter { PhysicalCatalogMatch.coordinateEquals(it.tableName, tableName) }
            .flatMap { mapColumns(it) }

    private fun mapColumns(table: SchemaTableWithFacets): List<ListColumnsItem> =
        table.columns.map { column ->
            val catalogPath = "${column.schemaName}.${column.tableName}.${column.columnName}"
            ListColumnsItem(
                schemaName = column.schemaName,
                tableName = column.tableName,
                columnName = column.columnName,
                displayName = displayName(column),
                description = description(column),
                nullable = column.dataType.nullability,
                type = column.dataType.type.typeId,
                catalogPath = catalogPath,
                metadataEntityUrn = MetadataEntityIds.resolve(catalogPath),
            )
        }

    /** Returns a warm snapshot when present and not expired; does not load. */
    private fun peekSnapshot(): SchemaFacetResult? =
        lock.read {
            val snap = cachedSnapshot ?: return@read null
            if (isExpired(cachedAtMillis)) return@read null
            snap
        }

    /** Returns a warm snapshot or loads and caches [SchemaFacetService.getSchemas]. */
    private fun snapshot(): SchemaFacetResult {
        peekSnapshot()?.let { return it }
        return lock.write {
            peekSnapshotUnlocked()?.let { return@write it }
            val loaded = svc.getSchemas()
            cachedSnapshot = loaded
            cachedAtMillis = System.currentTimeMillis()
            loaded
        }
    }

    private fun peekSnapshotUnlocked(): SchemaFacetResult? {
        val snap = cachedSnapshot ?: return null
        if (isExpired(cachedAtMillis)) return null
        return snap
    }

    private fun isExpired(loadedAtMillis: Long): Boolean {
        if (ttlMillis <= 0L) return false
        return System.currentTimeMillis() - loadedAtMillis >= ttlMillis
    }

    companion object {
        /** Default cache TTL — balances agent-turn reuse against post-capture facet freshness. */
        const val DEFAULT_TTL_MILLIS: Long = 30_000L
    }
}

/**
 * Adapts this [SchemaFacetService] to [SchemaCatalogPort] for non-Spring call sites (CLI, tests).
 *
 * @param ttlMillis cache TTL for the full catalog snapshot
 */
fun SchemaFacetService.asSchemaCatalogPort(
    ttlMillis: Long = SchemaFacetCatalogAdapter.DEFAULT_TTL_MILLIS,
): SchemaCatalogPort = SchemaFacetCatalogAdapter(this, ttlMillis)
