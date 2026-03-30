package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.domain.ImportMode
import io.qpointz.mill.metadata.repository.MetadataSeedLedgerRepository
import io.qpointz.mill.metadata.repository.SeedCompletionMetadata
import io.qpointz.mill.metadata.service.MetadataImportService
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import java.io.ByteArrayInputStream

/**
 * Applies configured `mill.metadata.seed.resources` with ledger-backed skips (SPEC §14.1).
 * Skips a resource when **`metadata_seed.fingerprint`** matches the current **MD5** of its bytes; re-imports on content change.
 *
 * @param seedProperties ordered resource locations and failure policy
 * @param importService canonical YAML import
 * @param ledger completion records (JPA in production; in-memory for file-only compositions)
 * @param resourceLoader resolves each seed location
 */
class MetadataSeedStartup(
    private val seedProperties: MetadataSeedProperties,
    private val importService: MetadataImportService,
    private val ledger: MetadataSeedLedgerRepository,
    private val resourceLoader: ResourceLoader
) {

    /**
     * Runs all configured seeds: skips when ledger fingerprint matches current MD5; otherwise imports via [MetadataImportService.import].
     *
     * @throws Exception first seed failure when `on-failure` is `fail-fast`
     */
    fun run() {
        val resources = seedProperties.resources
        if (resources.isEmpty()) {
            return
        }
        val failFast = !"continue".equals(seedProperties.onFailure, ignoreCase = true)
        for (location in resources) {
            val loc = location.trim()
            if (loc.isEmpty()) {
                continue
            }
            val seedKey = MetadataSeedKey.stableKey(resourceLoader, loc)
            val bytes = resourceLoader.getResource(loc).inputStream.use { it.readBytes() }
            val fingerprint = MetadataSeedContentFingerprint.md5Fingerprint(bytes)
            val existing = ledger.findBySeedKey(seedKey)
            if (existing?.completedAt != null && existing.fingerprint == fingerprint) {
                log.info("Skipping unchanged metadata seed [{}] {} ({})", seedKey, loc, fingerprint)
                continue
            }
            if (existing?.completedAt != null) {
                log.info(
                    "Re-applying metadata seed [{}] {} (content changed: was {}, now {})",
                    seedKey,
                    loc,
                    existing.fingerprint ?: "(none)",
                    fingerprint
                )
            }
            try {
                ByteArrayInputStream(bytes).use { stream ->
                    importService.import(stream, ImportMode.MERGE, actorId = "system")
                }
                ledger.recordCompletion(seedKey, SeedCompletionMetadata(fingerprint = fingerprint))
                log.info("Applied metadata seed [{}] {} ({})", seedKey, loc, fingerprint)
            } catch (ex: Exception) {
                log.error("Metadata seed failed [{}] {}", seedKey, loc, ex)
                if (failFast) {
                    throw ex
                }
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(MetadataSeedStartup::class.java)
    }
}
