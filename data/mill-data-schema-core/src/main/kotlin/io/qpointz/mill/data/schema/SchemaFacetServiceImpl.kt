package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.core.ConceptFacet
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.metadata.domain.core.RelationFacet
import io.qpointz.mill.metadata.domain.core.StructuralFacet
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet
import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.metadata.service.MetadataContext
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Table

/**
 * Default implementation of [SchemaFacetService].
 *
 * Matches metadata entities to physical schema coordinates by comparing
 * [MetadataEntity.schemaName], [MetadataEntity.tableName], and [MetadataEntity.attributeName]
 * against the physical schema produced by [SchemaProvider].
 *
 * Metadata entities that do not match any physical coordinate are collected
 * into [SchemaFacetResult.unboundMetadata].
 *
 * Facet resolution follows metadata-service semantics: each known facet type is resolved using
 * the ordered list of scopes from [MetadataContext], where the last matching scope wins.
 */
class SchemaFacetServiceImpl(
    private val schemaProvider: SchemaProvider,
    private val metadataRepository: MetadataRepository
) : SchemaFacetService {

    /** @see SchemaFacetService.getSchemas */
    override fun getSchemas(context: MetadataContext): SchemaFacetResult {
        val allEntities = metadataRepository.findAll()
        val usedEntityIds = mutableSetOf<String>()

        val schemas = schemaProvider.getSchemaNames().map { schemaName ->
            buildSchemaWithFacets(schemaName, allEntities, usedEntityIds, context)
        }

        val unboundMetadata = allEntities.filter { it.id != null && it.id !in usedEntityIds }

        return SchemaFacetResult(schemas = schemas, unboundMetadata = unboundMetadata)
    }

    /** @see SchemaFacetService.getSchema */
    override fun getSchema(schemaName: String, context: MetadataContext): SchemaWithFacets? {
        val schemaNames = schemaProvider.getSchemaNames()
        if (!schemaNames.contains(schemaName)) return null
        val allEntities = metadataRepository.findAll()
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

    /**
     * Fetches the physical schema for [schemaName], finds a matching schema-level metadata entity
     * (one with no tableName or attributeName), and recurses into tables.
     * Records the matched entity id in [usedEntityIds] to exclude it from unbound tracking.
     */
    private fun buildSchemaWithFacets(
        schemaName: String,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext
    ): SchemaWithFacets {
        val physicalSchema = schemaProvider.getSchema(schemaName)
        val schemaEntity = allEntities.find {
            coordinateEquals(it.schemaName, schemaName) && it.tableName == null && it.attributeName == null
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

    /**
     * Finds a table-level metadata entity matching [schemaName] + [table].name (attributeName must be null),
     * builds all columns, and returns a [SchemaTableWithFacets].
     * Records the matched entity id in [usedEntityIds].
     */
    private fun buildTableWithFacets(
        schemaName: String,
        table: Table,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext
    ): SchemaTableWithFacets {
        val tableEntity = allEntities.find {
            coordinateEquals(it.schemaName, schemaName) &&
                coordinateEquals(it.tableName, table.name) &&
                it.attributeName == null
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

    /**
     * Finds a column-level metadata entity matching all three coordinates
     * ([schemaName], [tableName], [field].name) and returns a [SchemaColumnWithFacets].
     * Records the matched entity id in [usedEntityIds].
     */
    private fun buildColumnWithFacets(
        schemaName: String,
        tableName: String,
        field: Field,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>,
        context: MetadataContext
    ): SchemaColumnWithFacets {
        val columnEntity = allEntities.find {
            coordinateEquals(it.schemaName, schemaName) &&
                coordinateEquals(it.tableName, tableName) &&
                coordinateEquals(it.attributeName, field.name)
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

    /**
     * Resolves all known platform facet types from [entity] under [context]
     * and returns them as a [SchemaFacets] holder.
     * Returns [SchemaFacets.EMPTY] when [entity] is null.
     */
    private fun buildFacets(entity: MetadataEntity?, context: MetadataContext): SchemaFacets {
        if (entity == null) return SchemaFacets.EMPTY
        val facets = mutableSetOf<MetadataFacet>()
        resolveFacet(entity, "descriptive", context, DescriptiveFacet::class.java)?.let { facets.add(it) }
        resolveFacet(entity, "structural", context, StructuralFacet::class.java)?.let { facets.add(it) }
        resolveFacet(entity, "relation", context, RelationFacet::class.java)?.let { facets.add(it) }
        resolveFacet(entity, "concept", context, ConceptFacet::class.java)?.let { facets.add(it) }
        resolveFacet(entity, "value-mapping", context, ValueMappingFacet::class.java)?.let { facets.add(it) }
        return SchemaFacets(facets)
    }

    /**
     * Resolves one facet type across the provided [context] scopes where the last matching scope
     * wins, matching metadata-service semantics.
     *
     * @param entity metadata entity carrying scoped facets
     * @param facetType short facet type key (for example, `descriptive`)
     * @param context ordered scope context
     * @param facetClass target facet class to deserialize
     * @return resolved facet instance, or null when no scope in the context has a value
     */
    private fun <T : MetadataFacet> resolveFacet(
        entity: MetadataEntity,
        facetType: String,
        context: MetadataContext,
        facetClass: Class<T>
    ): T? {
        var resolved: T? = null
        context.scopes.forEach { scope ->
            resolveFacetTypeCandidates(facetType).forEach { facetTypeCandidate ->
                resolveScopeCandidates(scope).forEach { scopeCandidate ->
                    entity.getFacet(facetTypeCandidate, scopeCandidate, facetClass).ifPresent { resolved = it }
                }
            }
        }
        return resolved
    }

    /**
     * Resolves scope lookup candidates for backward compatibility with legacy short keys
     * (`global`, `user:alice`) still present in older metadata stores.
     *
     * @param scope normalized scope key from [MetadataContext]
     * @return ordered candidate list where later entries have higher precedence
     */
    private fun resolveScopeCandidates(scope: String): List<String> {
        if (!scope.startsWith(MetadataUrns.SCOPE_PREFIX)) {
            return listOf(scope, MetadataUrns.normaliseScopePath(scope))
        }
        val shortKey = scope.removePrefix(MetadataUrns.SCOPE_PREFIX)
        return listOf(shortKey, scope)
    }

    /**
     * Resolves facet type lookup candidates for compatibility with both legacy short keys
     * and URN-normalized facet type keys persisted by metadata import pipelines.
     */
    private fun resolveFacetTypeCandidates(facetType: String): List<String> {
        if (facetType.startsWith(MetadataUrns.FACET_TYPE_PREFIX)) {
            return listOf(facetType)
        }
        return listOf(facetType, MetadataUrns.normaliseFacetTypePath(facetType))
    }

    /**
     * Compares physical/metadata coordinates case-insensitively because JDBC providers can expose
     * upper-case identifiers while metadata seeds commonly use lower-case names.
     */
    private fun coordinateEquals(left: String?, right: String?): Boolean {
        if (left == null || right == null) return left == right
        return left.equals(right, ignoreCase = true)
    }
}
