package io.qpointz.mill.metadata.configuration

import org.springframework.core.io.ResourceLoader

/**
 * Builds a stable **`metadata_seed.seed_key`** from a configured Spring resource location.
 *
 * <p>Keys are **not** derived from the list index in `mill.metadata.seed.resources`, so reordering
 * the list does not create new ledger rows or re-run completed seeds.
 *
 * <p>For **file-system** resources that exist and are regular files, the key is the **`file:` URI**
 * of the **canonical** path (resolves `..`, symlinks, and relative vs absolute spellings). For
 * `classpath:`, HTTP, or missing resources, the trimmed location string is used as-is.
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
                !r.exists() || !r.isFile -> trimmed
                else -> r.file.canonicalFile.toURI().toString()
            }
        } catch (_: Exception) {
            trimmed
        }
    }
}
