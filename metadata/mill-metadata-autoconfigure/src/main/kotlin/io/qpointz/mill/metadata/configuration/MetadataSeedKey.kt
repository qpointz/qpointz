package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.resource.MillConfigurationResourceKey
import org.springframework.core.io.ResourceLoader

/**
 * Builds a stable **`metadata_seed.seed_key`** from a configured Spring resource location.
 *
 * Keys are **not** derived from the list index in `mill.metadata.seed.resources`, so reordering
 * the list does not create new ledger rows or re-run completed seeds.
 *
 * For **file-system** resources that exist and are regular files, the key is the **`file:` URI**
 * of the **canonical** path (resolves `..`, symlinks, and relative vs absolute spellings). For
 * `classpath:`, HTTP, or missing resources, the trimmed location string is used as-is unless a
 * cloud-specific normalization applies.
 */
object MetadataSeedKey {

    /**
     * Returns a stable ledger key for [location].
     *
     * @param resourceLoader used to resolve [location] the same way as the seed runner
     * @param location Spring resource location from configuration (trimmed)
     * @return key to store in `metadata_seed.seed_key` (max length depends on path; column is 512)
     */
    @JvmStatic
    fun stableKey(resourceLoader: ResourceLoader, location: String): String {
        val trimmed = location.trim()
        if (trimmed.isEmpty()) {
            return trimmed
        }
        return try {
            val r = resourceLoader.getResource(trimmed)
            when {
                r is MillConfigurationResourceKey -> r.millConfigurationStableKey()
                isCloudSeedLocation(trimmed) -> normalizeCloudSeedLocationForKey(trimmed)
                !r.exists() || !r.isFile -> trimmed
                else -> r.file.canonicalFile.toURI().toString()
            }
        } catch (_: Exception) {
            if (isCloudSeedLocation(trimmed)) {
                normalizeCloudSeedLocationForKey(trimmed)
            } else {
                trimmed
            }
        }
    }

    private fun isCloudSeedLocation(loc: String): Boolean {
        val t = loc.trim().lowercase()
        return t.startsWith("s3://") || t.startsWith("gs://") || t.startsWith("azure-blob://")
    }

    /**
     * Strips volatile query strings and URL user-info so keys stay credential-free.
     */
    private fun normalizeCloudSeedLocationForKey(loc: String): String {
        var s = loc.trim()
        val q = s.indexOf('?')
        if (q >= 0) {
            s = s.substring(0, q)
        }
        val schemeSep = s.indexOf("://")
        if (schemeSep < 0) {
            return s
        }
        val at = s.indexOf('@', schemeSep + 3)
        if (at < 0) {
            return s
        }
        return s.substring(0, schemeSep + 3) + s.substring(at + 1)
    }
}
