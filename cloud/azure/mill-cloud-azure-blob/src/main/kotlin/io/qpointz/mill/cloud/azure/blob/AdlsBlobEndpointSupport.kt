package io.qpointz.mill.cloud.azure.blob

import java.net.URI

/**
 * Normalizes Azure blob service URLs for the Blob SDK and derives the storage account name
 * from well-known public DNS host patterns.
 */
internal object AdlsBlobEndpointSupport {

    /**
     * Blob service base URL (no `?query`) and optional SAS query string (without leading `?`).
     *
     * @param endpoint blob service URL; may include an embedded SAS query (`?sv=...&sig=...`)
     * @param sasToken optional SAS from `auth.sasToken` (must not duplicate embedded SAS)
     */
    fun resolveServiceUrlAndSas(endpoint: String, sasToken: String?): Result<Pair<String, String?>> {
        val trimmed = endpoint.trim()
        val embedded = parseEmbeddedSas(trimmed)
        val baseFromEmbedded = embedded?.first ?: stripQuery(trimmed)
        val embeddedQuery = embedded?.second
        val explicitSas = sasToken?.trim()?.takeIf { it.isNotBlank() }?.let { stripLeadingQuestion(it) }
        return when {
            explicitSas != null && embeddedQuery != null ->
                Result.failure(
                    IllegalArgumentException(
                        "Use SAS either in storage.endpoint query string or in storage.auth.sasToken, not both"
                    )
                )
            explicitSas != null -> Result.success(Pair(toBlobServiceUrl(baseFromEmbedded), explicitSas))
            embeddedQuery != null -> Result.success(Pair(toBlobServiceUrl(baseFromEmbedded), embeddedQuery))
            else -> Result.success(Pair(toBlobServiceUrl(baseFromEmbedded), null))
        }
    }

    private fun parseEmbeddedSas(trimmed: String): Pair<String, String>? {
        val uri = runCatching { URI(trimmed) }.getOrNull() ?: return null
        val rawQuery = uri.rawQuery ?: return null
        if (!looksLikeSasQuery(rawQuery)) return null
        val base = buildString {
            append(uri.scheme).append("://").append(uri.host)
            if (uri.port != -1) append(":").append(uri.port)
            uri.rawPath?.takeIf { it.isNotBlank() && it != "/" }?.let { append(it) }
        }
        return Pair(base, stripLeadingQuestion(rawQuery))
    }

    private fun looksLikeSasQuery(q: String): Boolean =
        q.contains("sig=", ignoreCase = true) ||
            (q.contains("se=", ignoreCase = true) && q.contains("sv=", ignoreCase = true))

    private fun stripQuery(url: String): String {
        val idx = url.indexOf('?')
        return if (idx >= 0) url.substring(0, idx) else url
    }

    private fun stripLeadingQuestion(s: String): String = s.removePrefix("?")

    /**
     * Converts an ADLS Gen2 DFS service URL to the equivalent blob service URL for
     * [com.azure.storage.blob.BlobServiceClient].
     */
    fun toBlobServiceUrl(serviceUrl: String): String {
        val trimmed = serviceUrl.trim()
        val u = runCatching { URI(trimmed) }.getOrNull() ?: return trimmed
        val h = u.host ?: return trimmed
        val blobHost = when {
            h.endsWith(".dfs.core.windows.net", ignoreCase = true) ->
                h.dropLast(".dfs.core.windows.net".length) + ".blob.core.windows.net"
            else -> h
        }
        if (blobHost == h) return trimmed
        return buildString {
            append(u.scheme).append("://").append(blobHost)
            if (u.port != -1) append(":").append(u.port)
            u.rawPath?.takeIf { it.isNotBlank() && it != "/" }?.let { append(it) }
        }
    }

    /**
     * Returns the storage account segment for `{account}.blob.core.windows.net` /
     * `{account}.dfs.core.windows.net`, or `null` for emulators and custom hosts.
     */
    fun storageAccountFromBlobOrDfsHost(serviceUrl: String): String? {
        val base = stripQuery(toBlobServiceUrl(serviceUrl))
        val uri = runCatching { URI(base) }.getOrNull() ?: return null
        val h = uri.host ?: return null
        val blobSuffix = ".blob.core.windows.net"
        if (h.endsWith(blobSuffix, ignoreCase = true)) {
            return h.dropLast(blobSuffix.length).takeIf { it.isNotBlank() }
        }
        val dfsSuffix = ".dfs.core.windows.net"
        if (h.endsWith(dfsSuffix, ignoreCase = true)) {
            return h.dropLast(dfsSuffix.length).takeIf { it.isNotBlank() }
        }
        return null
    }
}
