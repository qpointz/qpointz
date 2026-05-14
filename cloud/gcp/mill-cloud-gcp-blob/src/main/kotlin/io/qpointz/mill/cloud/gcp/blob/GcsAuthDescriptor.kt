package io.qpointz.mill.cloud.gcp.blob

/**
 * Authentication descriptor for Google Cloud Storage.
 *
 * Exactly one of the delegated credential fields ([accessToken], [serviceAccountJson],
 * [serviceAccountJsonPath]) may be non-blank. If all are blank (or the entire `auth`
 * block is omitted), Google Application Default Credentials (ADC) are used.
 *
 * When [preferAmbientCredentials] is `true`, ADC is used regardless of any delegated
 * credential fields.
 *
 * @property accessToken            OAuth 2.0 bearer token
 * @property serviceAccountJson     full service-account JSON key inline
 * @property serviceAccountJsonPath filesystem path to a service-account JSON key file
 * @property preferAmbientCredentials when `true`, forces ADC even if delegated fields are set
 */
data class GcsAuthDescriptor(
    val accessToken: String? = null,
    val serviceAccountJson: String? = null,
    val serviceAccountJsonPath: String? = null,
    val preferAmbientCredentials: Boolean = false
) {

    /**
     * Returns the names of all non-blank delegated credential fields.
     */
    fun activeDelegatedBundles(): List<String> = buildList {
        if (!accessToken.isNullOrBlank()) add("accessToken")
        if (!serviceAccountJson.isNullOrBlank()) add("serviceAccountJson")
        if (!serviceAccountJsonPath.isNullOrBlank()) add("serviceAccountJsonPath")
    }
}
