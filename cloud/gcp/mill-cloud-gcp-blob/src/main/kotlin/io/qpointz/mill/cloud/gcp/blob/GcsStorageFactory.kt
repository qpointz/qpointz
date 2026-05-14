package io.qpointz.mill.cloud.gcp.blob

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.StorageOptions
import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.descriptor.StorageDescriptor
import io.qpointz.mill.source.factory.StorageFactory
import java.io.ByteArrayInputStream
import java.io.FileInputStream

/**
 * [StorageFactory] that creates a [GcsBlobSource] from a [GcsStorageDescriptor].
 *
 * The Google Cloud Storage HTTP transport is **not** initialized until
 * [create] is called, keeping the Spring application-context refresh free
 * of remote I/O.
 */
class GcsStorageFactory : StorageFactory {

    companion object {
        /**
         * OAuth scope required for listing and reading objects via the Cloud Storage JSON API.
         * Service account credentials loaded from JSON must be scoped before use with some
         * client configurations; otherwise token exchange can fail with 401 Unauthorized.
         */
        private val STORAGE_READ_ONLY_SCOPES = listOf("https://www.googleapis.com/auth/devstorage.read_only")
    }

    override val descriptorType: Class<out StorageDescriptor>
        get() = GcsStorageDescriptor::class.java

    override fun create(descriptor: StorageDescriptor): BlobSource {
        require(descriptor is GcsStorageDescriptor) {
            "Expected GcsStorageDescriptor, got ${descriptor::class.java.name}"
        }

        val builder = StorageOptions.newBuilder()

        descriptor.projectId?.takeIf { it.isNotBlank() }?.let { builder.setProjectId(it) }
        descriptor.endpoint?.takeIf { it.isNotBlank() }?.let { builder.setHost(it) }

        val credentials = resolveCredentials(descriptor.auth)
        if (credentials != null) {
            builder.setCredentials(credentials)
        }

        val storage = builder.build().service
        return GcsBlobSource(storage, descriptor.bucket, descriptor.prefix)
    }

    /**
     * Resolves [GoogleCredentials] from the auth descriptor.
     *
     * @return resolved credentials, or `null` to let the client use its default
     */
    private fun resolveCredentials(auth: GcsAuthDescriptor?): GoogleCredentials? {
        if (auth == null) return null

        if (auth.preferAmbientCredentials) {
            return GoogleCredentials.getApplicationDefault()
        }

        return when {
            !auth.accessToken.isNullOrBlank() -> {
                GoogleCredentials.create(AccessToken(auth.accessToken, null))
            }

            !auth.serviceAccountJson.isNullOrBlank() -> {
                ServiceAccountCredentials.fromStream(
                    ByteArrayInputStream(auth.serviceAccountJson.toByteArray(Charsets.UTF_8))
                ).createScoped(STORAGE_READ_ONLY_SCOPES)
            }

            !auth.serviceAccountJsonPath.isNullOrBlank() -> {
                FileInputStream(auth.serviceAccountJsonPath).use { input ->
                    ServiceAccountCredentials.fromStream(input).createScoped(STORAGE_READ_ONLY_SCOPES)
                }
            }

            else -> null
        }
    }
}
