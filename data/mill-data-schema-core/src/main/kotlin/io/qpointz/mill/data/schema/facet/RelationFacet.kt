package io.qpointz.mill.data.schema.facet

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.metadata.domain.RelationType
import io.qpointz.mill.metadata.domain.core.TableLocator

/**
 * JDBC- and schema-facing relationship facet: typed interpretation of stored relation payloads.
 *
 * Stored JSON remains grammar-validated in metadata; this type is the schema-layer binding for
 * deserialization and UI/schema exploration.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RelationFacet(
    val relations: List<Relation> = emptyList(),
    override val facetType: String = "relation",
) : MetadataFacet {

    /** Immutable relation entry inside [RelationFacet]. */
    data class Relation(
        val name: String? = null,
        val description: String? = null,
        val sourceTable: TableLocator? = null,
        val sourceAttributes: List<String> = emptyList(),
        val targetTable: TableLocator? = null,
        val targetAttributes: List<String> = emptyList(),
        val cardinality: RelationCardinality? = null,
        val type: RelationType? = null,
        val joinSql: String? = null,
        val businessMeaning: String? = null,
    )

    /** Returns relations where source or target table matches given table. */
    fun getRelationsForEntity(schema: String, table: String): List<Relation> =
        relations.filter {
            it.sourceTable?.matches(schema, table, null) == true ||
                it.targetTable?.matches(schema, table, null) == true
        }
}
