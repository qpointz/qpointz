package io.qpointz.mill.metadata.repository

import java.time.Instant

/**
 * Record of a completed startup seed application (SPEC §6.6 / §14.1).
 *
 * @property seedKey stable unique key for the seed resource (location-based; not the list index in configuration)
 * @property completedAt when the seed finished successfully
 * @property fingerprint content digest for change detection (e.g. **`md5:`** + hex), or null for rows created before hashing
 * @property lastError optional error text from the last failed attempt
 */
data class SeedLedgerEntry(
    val seedKey: String,
    val completedAt: Instant?,
    val fingerprint: String?,
    val lastError: String?
)

/**
 * Metadata supplied when marking a seed as completed (SPEC §6.6).
 *
 * @property fingerprint optional content fingerprint
 * @property lastError optional error message when recording a failed attempt (implementation-defined)
 */
data class SeedCompletionMetadata(
    val fingerprint: String? = null,
    val lastError: String? = null
)

/**
 * Ledger for `mill.metadata.seed.*` resources (SPEC §6.6).
 *
 * JPA implementation lives in `mill-metadata-persistence`; the startup runner in autoconfigure
 * depends on this interface only.
 */
interface MetadataSeedLedgerRepository {

    /**
     * @param seedKey stable seed identifier
     * @return existing ledger row or null
     */
    fun findBySeedKey(seedKey: String): SeedLedgerEntry?

    /**
     * Records successful completion for [seedKey], inserting a row or updating **`fingerprint`** /
     * **`completed_at`** when the resource content changed.
     *
     * @param seedKey stable seed identifier (location-based; not list index)
     * @param metadata [SeedCompletionMetadata.fingerprint] content digest for change detection (e.g. `md5:` + hex)
     */
    fun recordCompletion(seedKey: String, metadata: SeedCompletionMetadata)
}
