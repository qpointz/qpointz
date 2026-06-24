package io.qpointz.mill.data.schema

import io.qpointz.mill.data.schema.facet.RelationFacet

/**
 * Merges table-level and schema-level relation facets into outbound relations for one physical
 * table, applying same-schema filtering and deduplication.
 */
object RelationFacetMaterializer {

    /**
     * Returns outbound relations for [table] within [schemaName], merging table facets with
     * schema-level relation declarations.
     *
     * @param schemaName OData service schema (both endpoints must match to be included)
     * @param table physical table with merged metadata facets
     * @param schema optional parent schema carrying schema-level relation facets
     * @return deduplicated outbound relations where [table] is the source
     */
    fun effectiveRelations(
        schemaName: String,
        table: SchemaTableWithFacets,
        schema: SchemaWithFacets?,
    ): List<RelationFacet.Relation> {
        val fromTable = table.facets.relation?.relations.orEmpty()
            .filter { isOutboundSameSchema(schemaName, table.tableName, it) }

        val fromSchema = schema?.facets?.relation?.relations.orEmpty()
            .filter { isOutboundSameSchema(schemaName, table.tableName, it) }

        return deduplicate(fromTable + fromSchema)
    }

    /**
     * Resolves the default OData navigation property name for a relation edge from [sourceTableName].
     *
     * @param sourceTableName physical table name hosting the navigation
     * @param relation canonical relation entry
     */
    fun navigationName(sourceTableName: String, relation: RelationFacet.Relation): String =
        relation.name?.takeIf { it.isNotBlank() }
            ?: "${sourceTableName}_${relation.targetTable?.table}"

    private fun isOutboundSameSchema(
        schemaName: String,
        tableName: String,
        relation: RelationFacet.Relation,
    ): Boolean {
        val source = relation.sourceTable ?: return false
        val target = relation.targetTable ?: return false
        if (!source.matches(schemaName, tableName, null)) {
            return false
        }
        return source.schema.equals(schemaName, ignoreCase = false) &&
            target.schema.equals(schemaName, ignoreCase = false)
    }

    private fun deduplicate(relations: List<RelationFacet.Relation>): List<RelationFacet.Relation> {
        val seen = LinkedHashSet<String>()
        val out = ArrayList<RelationFacet.Relation>(relations.size)
        for (relation in relations) {
            val key = relationKey(relation)
            if (seen.add(key)) {
                out += relation
            }
        }
        return out
    }

    private fun relationKey(relation: RelationFacet.Relation): String {
        val src = relation.sourceTable
        val tgt = relation.targetTable
        return "${src?.schema}.${src?.table}|${relation.sourceAttributes}|" +
            "${tgt?.schema}.${tgt?.table}|${relation.targetAttributes}"
    }
}
