package io.qpointz.mill.persistence.metadata.jpa.repositories

import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataSeedLedgerEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/** Spring Data repository for [MetadataSeedLedgerEntity] (`metadata_seed`). */
interface MetadataSeedLedgerJpaRepository : JpaRepository<MetadataSeedLedgerEntity, Long> {

    fun findBySeedKey(seedKey: String): Optional<MetadataSeedLedgerEntity>
}
