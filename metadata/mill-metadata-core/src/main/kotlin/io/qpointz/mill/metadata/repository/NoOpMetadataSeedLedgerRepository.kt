package io.qpointz.mill.metadata.repository

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [MetadataSeedLedgerRepository] for tests and non-persistent compositions.
 */
class NoOpMetadataSeedLedgerRepository : MetadataSeedLedgerRepository {

    private val entries = ConcurrentHashMap<String, SeedLedgerEntry>()

    override fun findBySeedKey(seedKey: String): SeedLedgerEntry? = entries[seedKey]

    override fun recordCompletion(seedKey: String, metadata: SeedCompletionMetadata) {
        val now = Instant.now()
        entries[seedKey] = SeedLedgerEntry(
            seedKey = seedKey,
            completedAt = now,
            fingerprint = metadata.fingerprint,
            lastError = metadata.lastError
        )
    }
}
