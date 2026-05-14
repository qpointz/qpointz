package io.qpointz.mill.source.descriptor

/**
 * Contract for [StorageDescriptor] implementations that can produce a redacted
 * metadata-safe representation of their configuration for inferred facets.
 *
 * Each implementation owns its own redaction rules: it knows which fields are
 * structural (safe to expose) and which are secrets.
 */
interface StorageFacetContributor {

    /**
     * Returns a JSON-safe map of storage parameters appropriate for the given [mode].
     *
     * - [StorageFacetRedactMode.NONE]: all fields as-is.
     * - [StorageFacetRedactMode.BASIC]: strip secrets, sanitize URLs, add hints like `authConfigured`.
     * - [StorageFacetRedactMode.SAFE]: only structural identity fields (bucket, container, prefix, etc.).
     *
     * The returned map should **not** include `type`; the caller adds the discriminator.
     */
    fun storageFacetParams(mode: StorageFacetRedactMode): Map<String, Any?>
}
