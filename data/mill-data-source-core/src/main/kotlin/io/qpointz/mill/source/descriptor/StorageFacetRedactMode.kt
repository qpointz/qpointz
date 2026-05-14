package io.qpointz.mill.source.descriptor

/**
 * Hygiene level for metadata facet payloads that include storage configuration.
 *
 * Bound from Spring Boot as `mill.data.backend.metadata.redact`.
 */
enum class StorageFacetRedactMode {
    /** Pass storage parameters through unchanged (may expose credentials). */
    NONE,

    /**
     * Strip delegated secrets (connection strings, keys, tokens, inline JSON), keep
     * non-sensitive structural fields and safe hints.
     */
    BASIC,

    /**
     * After basic hygiene, retain only a small allow-listed subset per storage type
     * (e.g. `bucket` / `container`, `prefix`, `region`, `rootPath`).
     */
    SAFE
}
