package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.core.ConceptFacet
import io.qpointz.mill.metadata.domain.core.DescriptiveFacet
import io.qpointz.mill.metadata.domain.core.RelationFacet
import io.qpointz.mill.metadata.domain.core.StructuralFacet
import io.qpointz.mill.metadata.domain.core.ValueMappingFacet
import io.qpointz.mill.metadata.repository.MetadataRepository
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
 * @param scope Facet scope used when resolving facets from matched entities. Defaults to "global".
 */
class SchemaFacetServiceImpl @JvmOverloads constructor(
    private val schemaProvider: SchemaProvider,
    private val metadataRepository: MetadataRepository,
    private val scope: String = "global"
) : SchemaFacetService {

    /** @see SchemaFacetService.getSchemas */
    override fun getSchemas(): SchemaFacetResult {
        val allEntities = metadataRepository.findAll()
        val usedEntityIds = mutableSetOf<String>()

        val schemas = schemaProvider.getSchemaNames().map { schemaName ->
            buildSchemaWithFacets(schemaName, allEntities, usedEntityIds)
        }

        val unboundMetadata = allEntities.filter { it.id != null && it.id !in usedEntityIds }

        return SchemaFacetResult(schemas = schemas, unboundMetadata = unboundMetadata)
    }

    /**
     * Fetches the physical schema for [schemaName], finds a matching schema-level metadata entity
     * (one with no tableName or attributeName), and recurses into tables.
     * Records the matched entity id in [usedEntityIds] to exclude it from unbound tracking.
     */
    private fun buildSchemaWithFacets(
        schemaName: String,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>
    ): SchemaWithFacets {
        val physicalSchema = schemaProvider.getSchema(schemaName)
        val schemaEntity = allEntities.find {
            it.schemaName == schemaName && it.tableName == null && it.attributeName == null
        }
        schemaEntity?.id?.let { usedEntityIds.add(it) }

        val tables = physicalSchema.tablesList.map { table ->
            buildTableWithFacets(schemaName, table, allEntities, usedEntityIds)
        }

        return SchemaWithFacets(
            schemaName = schemaName,
            tables = tables,
            metadata = schemaEntity,
            facets = buildFacets(schemaEntity)
        )
    }

    /**
     * Finds a table-level metadata entity matching [schemaName] + [table].name (attributeName must be null),
     * builds all attributes, and returns a [SchemaTableWithFacets].
     * Records the matched entity id in [usedEntityIds].
     */
    private fun buildTableWithFacets(
        schemaName: String,
        table: Table,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>
    ): SchemaTableWithFacets {
        val tableEntity = allEntities.find {
            it.schemaName == schemaName && it.tableName == table.name && it.attributeName == null
        }
        tableEntity?.id?.let { usedEntityIds.add(it) }

        val attributes = table.fieldsList.map { field ->
            buildAttributeWithFacets(schemaName, table.name, field, allEntities, usedEntityIds)
        }

        return SchemaTableWithFacets(
            schemaName = schemaName,
            tableName = table.name,
            tableType = table.tableType,
            attributes = attributes,
            metadata = tableEntity,
            facets = buildFacets(tableEntity)
        )
    }

    /**
     * Finds an attribute-level metadata entity matching all three coordinates
     * ([schemaName], [tableName], [field].name) and returns a [SchemaAttributeWithFacets].
     * Records the matched entity id in [usedEntityIds].
     */
    private fun buildAttributeWithFacets(
        schemaName: String,
        tableName: String,
        field: Field,
        allEntities: List<MetadataEntity>,
        usedEntityIds: MutableSet<String>
    ): SchemaAttributeWithFacets {
        val attrEntity = allEntities.find {
            it.schemaName == schemaName && it.tableName == tableName && it.attributeName == field.name
        }
        attrEntity?.id?.let { usedEntityIds.add(it) }

        return SchemaAttributeWithFacets(
            schemaName = schemaName,
            tableName = tableName,
            attributeName = field.name,
            fieldIndex = field.fieldIdx,
            dataType = field.type,
            metadata = attrEntity,
            facets = buildFacets(attrEntity)
        )
    }

    /**
     * Resolves all known platform facet types from [entity] under the configured [scope]
     * and returns them as a [SchemaFacets] holder.
     * Returns [SchemaFacets.EMPTY] when [entity] is null.
     */
    private fun buildFacets(entity: MetadataEntity?): SchemaFacets {
        if (entity == null) return SchemaFacets.EMPTY
        val facets = mutableSetOf<MetadataFacet>()
        entity.getFacet("descriptive",   scope, DescriptiveFacet::class.java).ifPresent  { facets.add(it) }
        entity.getFacet("structural",    scope, StructuralFacet::class.java).ifPresent   { facets.add(it) }
        entity.getFacet("relation",      scope, RelationFacet::class.java).ifPresent     { facets.add(it) }
        entity.getFacet("concept",       scope, ConceptFacet::class.java).ifPresent      { facets.add(it) }
        entity.getFacet("value-mapping", scope, ValueMappingFacet::class.java).ifPresent { facets.add(it) }
        return SchemaFacets(facets)
    }
}
