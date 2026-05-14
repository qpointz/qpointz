package io.qpointz.mill.data.backend.flow

import io.qpointz.mill.source.descriptor.StorageFacetRedactMode

/**
 * **Fallback** redactor for storage maps that are not handled by
 * [io.qpointz.mill.source.descriptor.StorageFacetContributor] implementations.
 *
 * Preferred path: each [io.qpointz.mill.source.descriptor.StorageDescriptor] implements
 * [io.qpointz.mill.source.descriptor.StorageFacetContributor] and owns its own redaction logic.
 * This object is used only when a descriptor does not implement the contributor contract.
 *
 * Input is the Jackson `convertValue` map **without** the `type` discriminator key.
 */
internal object StorageFacetPayloadRedactor {

    private val secretAuthKeys: Set<String> =
        setOf(
            "accountKey",
            "sasToken",
            "accessKeyId",
            "secretAccessKey",
            "accessKey",
            "secretKey",
            "sessionToken",
            "accessToken",
            "serviceAccountJson",
            "serviceAccountJsonPath",
        )

    private val basicAuthPassthroughKeys: Set<String> =
        setOf(
            "accountName",
            "preferAmbientCredentials",
        )

    fun redact(type: String, params: Map<String, Any?>, mode: StorageFacetRedactMode): Map<String, Any?> {
        return when (mode) {
            StorageFacetRedactMode.NONE -> LinkedHashMap(params)
            StorageFacetRedactMode.BASIC -> basic(params)
            StorageFacetRedactMode.SAFE -> safe(type, basic(params))
        }
    }

    private fun basic(params: Map<String, Any?>): LinkedHashMap<String, Any?> {
        val out = LinkedHashMap<String, Any?>()
        var connectionStringConfigured = false
        for ((key, value) in params) {
            when (key) {
                "connectionString" -> {
                    if (!isBlankish(value)) {
                        connectionStringConfigured = true
                    }
                }
                "auth" -> {
                    val (redactedAuth, hadSecrets) = redactAuthBlock(value)
                    if (redactedAuth != null && !isAuthOnlyDefaultPreferAmbient(redactedAuth)) {
                        out["auth"] = redactedAuth
                    }
                    if (hadSecrets) {
                        out["delegatedAuthConfigured"] = true
                    }
                }
                "endpoint" ->
                    if (value is String) {
                        out[key] = sanitizeEndpointForDisplay(value)
                    } else {
                        out[key] = value
                    }
                else -> out[key] = value
            }
        }
        if (connectionStringConfigured) {
            out["connectionStringConfigured"] = true
        }
        return out
    }

    private fun safe(type: String, basicParams: Map<String, Any?>): LinkedHashMap<String, Any?> {
        val allow =
            when (type) {
                "local" -> setOf("rootPath")
                "s3" -> setOf("bucket", "prefix", "region", "requesterPays")
                "gcs" -> setOf("bucket", "prefix", "projectId")
                "adls" -> setOf("container", "prefix")
                else -> setOf("prefix")
            }
        val out = LinkedHashMap<String, Any?>()
        for (k in allow) {
            basicParams[k]?.let { out[k] = it }
        }
        basicParams["delegatedAuthConfigured"]?.let { out["delegatedAuthConfigured"] = it }
        basicParams["connectionStringConfigured"]?.let { out["connectionStringConfigured"] = it }
        return out
    }

    private fun isAuthOnlyDefaultPreferAmbient(auth: Map<String, Any?>): Boolean =
        auth.size == 1 &&
            auth.containsKey("preferAmbientCredentials") &&
            auth["preferAmbientCredentials"] == false

    private fun redactAuthBlock(value: Any?): Pair<Map<String, Any?>?, Boolean> {
        if (value !is Map<*, *>) {
            return null to false
        }
        @Suppress("UNCHECKED_CAST")
        val m = value as Map<String, Any?>
        val out = LinkedHashMap<String, Any?>()
        var hadSecrets = false
        for ((rawKey, v) in m) {
            val key = rawKey.toString()
            when {
                key in secretAuthKeys -> {
                    if (!isBlankish(v)) {
                        hadSecrets = true
                    }
                }
                key in basicAuthPassthroughKeys -> out[key] = v
                else -> {
                    if (!isBlankish(v)) {
                        hadSecrets = true
                    }
                }
            }
        }
        return (if (out.isEmpty()) null else out) to hadSecrets
    }

    private fun sanitizeEndpointForDisplay(url: String): String {
        val q = url.indexOf('?')
        if (q < 0) {
            return url
        }
        val query = url.substring(q + 1)
        val looksLikeSas =
            query.contains("sig=", ignoreCase = true) ||
                (query.contains("se=", ignoreCase = true) && query.contains("sv=", ignoreCase = true))
        return if (looksLikeSas) url.substring(0, q) else url
    }

    private fun isBlankish(value: Any?): Boolean =
        value == null || (value is String && value.isBlank())
}
