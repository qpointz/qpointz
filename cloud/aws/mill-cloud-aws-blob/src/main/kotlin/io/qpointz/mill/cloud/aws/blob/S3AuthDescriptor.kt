package io.qpointz.mill.cloud.aws.blob

import io.qpointz.mill.source.verify.*

/**
 * Authentication configuration for an S3 storage backend.
 *
 * Three credential modes are supported:
 *
 * 1. **Delegated static keys** — caller supplies [accessKeyId] and [secretAccessKey]
 *    (and optionally [sessionToken]). The factory builds explicit credentials.
 * 2. **Ambient** — all key fields are blank/null and [preferAmbientCredentials] is
 *    `false` (default). The AWS SDK default credential provider chain is used.
 * 3. **Forced ambient** — [preferAmbientCredentials] is `true`. Even if static keys
 *    are supplied they are ignored; the SDK default chain is used instead.
 *
 * Partial bundles (one of [accessKeyId] / [secretAccessKey] present, the other blank)
 * produce a [Severity.ERROR] during verification.
 *
 * @property accessKeyId          AWS access key ID (null/blank → ambient)
 * @property secretAccessKey      AWS secret access key (null/blank → ambient)
 * @property sessionToken         optional session token for temporary credentials
 * @property preferAmbientCredentials when `true`, forces ambient regardless of keys
 */
data class S3AuthDescriptor(
    val accessKeyId: String? = null,
    val secretAccessKey: String? = null,
    val sessionToken: String? = null,
    val preferAmbientCredentials: Boolean = false
) {

    /**
     * Whether delegated static credentials should be used.
     *
     * Returns `true` when both [accessKeyId] and [secretAccessKey] are non-blank
     * and [preferAmbientCredentials] is `false`.
     */
    val useDelegatedCredentials: Boolean
        get() = !preferAmbientCredentials
                && !accessKeyId.isNullOrBlank()
                && !secretAccessKey.isNullOrBlank()

    /**
     * Validates auth configuration, returning issues for partial key bundles.
     *
     * @return list of verification issues (may be empty)
     */
    fun verify(): List<VerificationIssue> {
        val issues = mutableListOf<VerificationIssue>()

        val hasKey = !accessKeyId.isNullOrBlank()
        val hasSecret = !secretAccessKey.isNullOrBlank()

        if (hasKey != hasSecret) {
            issues += VerificationIssue(
                severity = Severity.ERROR,
                phase = Phase.DESCRIPTOR,
                message = "Partial auth bundle: both 'accessKeyId' and 'secretAccessKey' " +
                        "must be provided together, or both left blank for ambient credentials"
            )
        }

        return issues
    }
}
