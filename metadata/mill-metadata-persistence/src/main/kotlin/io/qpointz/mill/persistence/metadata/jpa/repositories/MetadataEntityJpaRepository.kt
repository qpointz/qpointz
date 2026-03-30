package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/** Spring Data repository for [MetadataEntityRecord] (`metadata_entity`). */
interface MetadataEntityJpaRepository : JpaRepository<MetadataEntityRecord, Long> {

    fun findByEntityRes(entityRes: String): Optional<MetadataEntityRecord>

    fun findByUuid(uuid: String): Optional<MetadataEntityRecord>

    fun existsByEntityRes(entityRes: String): Boolean

    fun deleteByEntityRes(entityRes: String)

    fun findByEntityKind(entityKind: String): List<MetadataEntityRecord>
}
