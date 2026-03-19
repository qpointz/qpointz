package io.qpointz.mill.persistence

/**
 * Shared relation-participant contract for persistence entities that can take part in
 * generic cross-model relations.
 *
 * Rules:
 * - [id] is the stable, opaque entity identifier (matches the entity table PK)
 * - [type] is a plain canonical string in the current contract
 * - [urn] is the persistence-level global identifier; format: `urn:<type-path>:<id>`
 *
 * Only entities implementing [EntityRef] may participate in [RelationRecord] relations.
 */
interface EntityRef {
    val id: String
    val type: String
    val urn: String
}
