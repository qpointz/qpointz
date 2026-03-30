package io.qpointz.mill.data.schema.facet

import io.qpointz.mill.metadata.domain.AbstractFacet
import io.qpointz.mill.metadata.domain.MetadataFacet
import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.metadata.domain.RelationType
import io.qpointz.mill.metadata.domain.ValidationException
import io.qpointz.mill.metadata.domain.core.TableLocator

/**
 * JDBC- and schema-facing relationship facet: typed interpretation of stored relation payloads.
 *
 * Stored JSON remains grammar-validated in metadata; this class is the schema-layer binding for
 * deserialization and UI/schema exploration.
 */
open class RelationFacet(
    var relations: MutableList<Relation> = mutableListOf()
) : AbstractFacet() {

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
        val businessMeaning: String? = null
    )

    override val facetType: String get() = "relation"

    override fun validate() {
        for (r in relations) {
            require(!r.name.isNullOrEmpty()) { throw ValidationException("RelationFacet: relation name is required") }
            requireNotNull(r.sourceTable) { throw ValidationException("RelationFacet: sourceTable is required for relation: ${r.name}") }
            requireNotNull(r.targetTable) { throw ValidationException("RelationFacet: targetTable is required for relation: ${r.name}") }
        }
    }

    override fun merge(other: MetadataFacet): MetadataFacet {
        if (other !is RelationFacet) return this
        if (other.relations.isNotEmpty()) {
            val merged = relations.toMutableList()
            for (otherR in other.relations) {
                if (merged.none { it.name == otherR.name }) merged.add(otherR)
            }
            relations = merged
        }
        return this
    }

    /** Returns relations where source or target table matches given table. */
    fun getRelationsForEntity(schema: String, table: String): List<Relation> =
        relations.filter {
            it.sourceTable?.matches(schema, table, null) == true ||
                it.targetTable?.matches(schema, table, null) == true
        }

    override fun equals(other: Any?): Boolean = this === other || (other is RelationFacet && relations == other.relations)
    override fun hashCode(): Int = relations.hashCode()
}
