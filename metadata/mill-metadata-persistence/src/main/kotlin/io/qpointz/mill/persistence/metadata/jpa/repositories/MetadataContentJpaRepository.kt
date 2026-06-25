package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataContentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/** Spring Data repository for [MetadataContentEntity] ({@code metadata_content}). */
interface MetadataContentJpaRepository : JpaRepository<MetadataContentEntity, Long> {

    fun findByContentUrn(contentUrn: String): Optional<MetadataContentEntity>

    fun findByTargetUrnAndContentKindOrderBySortOrderAscContentUrnAsc(
        targetUrn: String,
        contentKind: String,
    ): List<MetadataContentEntity>

    fun findByTargetUrnOrderBySortOrderAscContentUrnAsc(targetUrn: String): List<MetadataContentEntity>
}
