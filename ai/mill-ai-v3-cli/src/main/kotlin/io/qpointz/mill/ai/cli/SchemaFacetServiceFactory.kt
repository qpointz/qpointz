package io.qpointz.mill.ai.cli

import io.qpointz.mill.data.schema.SchemaFacetService

/**
 * Factory for creating [SchemaFacetService] instances in the CLI.
 *
 * ## Option A — demo (current)
 * [demo] returns an in-memory retail schema with no external dependencies.
 * Use this for interactive CLI testing and local development.
 *
 * ## Option B — skymill (future)
 * Add [fromSkymill] here when a live skymill endpoint is available:
 * ```kotlin
 * fun fromSkymill(endpoint: String, token: String): SchemaFacetService =
 *     SkymillSchemaFacetService(endpoint, token)
 * ```
 * The CLI selects the implementation via the [create] entry point,
 * driven by the `SCHEMA_SOURCE` environment variable.
 */
object SchemaFacetServiceFactory {

    /**
     * Create a [SchemaFacetService] based on the `SCHEMA_SOURCE` environment variable.
     *
     * - `SCHEMA_SOURCE=demo` (default) → [demo]
     * - `SCHEMA_SOURCE=skymill`        → reserved for Option B
     */
    fun create(): SchemaFacetService {
        return when (val source = System.getenv("SCHEMA_SOURCE") ?: "demo") {
            "demo"     -> demo()
            // Option B: "skymill" -> fromSkymill(...)
            else -> error("Unknown SCHEMA_SOURCE: $source. Supported values: demo")
        }
    }

    /** In-memory demo retail schema — no external dependencies. */
    fun demo(): SchemaFacetService = DemoSchemaFacetService()
}

