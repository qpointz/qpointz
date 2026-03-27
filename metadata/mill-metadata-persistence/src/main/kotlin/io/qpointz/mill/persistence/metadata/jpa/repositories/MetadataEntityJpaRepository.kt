package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/**
 * Spring Data JPA repository for [MetadataEntityRecord].
 *
 * Provides CRUD operations on `metadata_entity` plus location-based lookup finders.
 */
interface MetadataEntityJpaRepository : JpaRepository<MetadataEntityRecord, Long> {

    /**
     * Finds a metadata entity by its business id / FQDN (`entity_res`).
     *
     * @param entityRes domain id string
     * @return the matching entity record, or empty if not found
     */
    fun findByEntityRes(entityRes: String): Optional<MetadataEntityRecord>

    /**
     * Returns whether a row exists for the given `entity_res`.
     *
     * @param entityRes domain id string
     * @return `true` if present
     */
    fun existsByEntityRes(entityRes: String): Boolean

    /**
     * Deletes the entity with the given business id.
     *
     * @param entityRes domain id string
     */
    fun deleteByEntityRes(entityRes: String)

    /**
     * Finds a metadata entity by its three-part location coordinates.
     *
     * @param schemaName    schema name coordinate; may be `null`
     * @param tableName     table name coordinate; may be `null`
     * @param attributeName attribute name coordinate; may be `null`
     * @return the matching entity record, or empty if not found
     */
    fun findBySchemaNameAndTableNameAndAttributeName(
        schemaName: String?,
        tableName: String?,
        attributeName: String?
    ): Optional<MetadataEntityRecord>

    /**
     * Finds all metadata entity records of the given entity type.
     *
     * @param entityType entity type string (e.g. `SCHEMA`, `TABLE`, `ATTRIBUTE`)
     * @return all records matching the entity type
     */
    fun findByEntityType(entityType: String): List<MetadataEntityRecord>
}
