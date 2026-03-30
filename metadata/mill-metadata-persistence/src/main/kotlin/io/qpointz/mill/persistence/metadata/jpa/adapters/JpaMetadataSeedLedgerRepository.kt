package io.qpointz.mill.persistence.metadata.jpa.adapters

import io.qpointz.mill.metadata.repository.MetadataSeedLedgerRepository
import io.qpointz.mill.metadata.repository.SeedCompletionMetadata
import io.qpointz.mill.metadata.repository.SeedLedgerEntry
import io.qpointz.mill.persistence.metadata.jpa.entities.MetadataSeedLedgerEntity
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataSeedLedgerJpaRepository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * @param jpa Spring Data repository for `metadata_seed`
 */
@Transactional
class JpaMetadataSeedLedgerRepository(
    private val jpa: MetadataSeedLedgerJpaRepository
) : MetadataSeedLedgerRepository {

    override fun findBySeedKey(seedKey: String): SeedLedgerEntry? =
        jpa.findBySeedKey(seedKey).map { toDomain(it) }.orElse(null)

    override fun recordCompletion(seedKey: String, metadata: SeedCompletionMetadata) {
        val now = Instant.now()
        val existing = jpa.findBySeedKey(seedKey).orElse(null)
        if (existing == null) {
            jpa.save(
                MetadataSeedLedgerEntity(
                    uuid = UUID.randomUUID().toString(),
                    seedKey = seedKey,
                    completedAt = now,
                    fingerprint = metadata.fingerprint,
                    lastError = metadata.lastError,
                    createdAt = now,
                    createdBy = "system",
                    lastModifiedAt = now,
                    lastModifiedBy = "system"
                )
            )
        } else {
            existing.completedAt = now
            existing.fingerprint = metadata.fingerprint
            existing.lastError = metadata.lastError
            existing.lastModifiedAt = now
            existing.lastModifiedBy = "system"
            jpa.save(existing)
        }
    }

    private fun toDomain(e: MetadataSeedLedgerEntity): SeedLedgerEntry = SeedLedgerEntry(
        seedKey = e.seedKey,
        completedAt = e.completedAt,
        fingerprint = e.fingerprint,
        lastError = e.lastError
    )
}
