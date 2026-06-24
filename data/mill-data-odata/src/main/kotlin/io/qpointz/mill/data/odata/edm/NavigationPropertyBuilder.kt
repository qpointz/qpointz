package io.qpointz.mill.data.odata.edm

import com.sdl.odata.api.edm.model.NavigationProperty
import com.sdl.odata.edm.model.NavigationPropertyImpl
import com.sdl.odata.edm.model.ReferentialConstraintImpl
import io.qpointz.mill.data.schema.RelationFacetMaterializer
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.data.schema.facet.RelationFacet
import io.qpointz.mill.metadata.domain.RelationCardinality

/**
 * Builds RWS navigation properties from materialized [RelationFacet] entries on physical tables.
 */
class NavigationPropertyBuilder {

    /**
     * @param table source physical table
     * @param relations outbound relations where [table] is the source (same-schema targets only)
     * @return navigation properties declared on the source table
     */
    fun buildForTable(
        table: SchemaTableWithFacets,
        relations: List<RelationFacet.Relation>,
    ): List<NavigationProperty> =
        relations.mapNotNull { relation ->
            val source = relation.sourceTable
            val target = relation.targetTable
            if (source == null || target == null) {
                return@mapNotNull null
            }
            if (!source.matches(table.schemaName, table.tableName, null)) {
                return@mapNotNull null
            }
            if (!target.schema.equals(table.schemaName, ignoreCase = false)) {
                return@mapNotNull null
            }
            val navName = RelationFacetMaterializer.navigationName(table.tableName, relation)
            val targetFqn = EntitySetNaming.entityTypeFqn(target.schema, target.table)
            val typeName = if (isCollection(relation.cardinality)) {
                "Collection($targetFqn)"
            } else {
                targetFqn
            }
            val constraints = buildReferentialConstraints(relation)
            NavigationPropertyImpl.Builder()
                .setName(navName)
                .setTypeName(typeName)
                .setIsNullable(true)
                .apply {
                    if (constraints.isNotEmpty()) {
                        addReferentialConstraints(constraints)
                    }
                }
                .build()
        }

    private fun isCollection(cardinality: RelationCardinality?): Boolean =
        cardinality == RelationCardinality.ONE_TO_MANY || cardinality == RelationCardinality.MANY_TO_MANY

    private fun buildReferentialConstraints(relation: RelationFacet.Relation): List<ReferentialConstraintImpl> {
        val sourceAttrs = relation.sourceAttributes
        val targetAttrs = relation.targetAttributes
        if (sourceAttrs.isEmpty() || sourceAttrs.size != targetAttrs.size) {
            return emptyList()
        }
        return sourceAttrs.zip(targetAttrs).map { (source, target) ->
            ReferentialConstraintImpl(source, target)
        }
    }
}
