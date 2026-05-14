package io.qpointz.mill.source.resolve

/**
 * SPI for resolving secret references from provider-qualified
 * placeholders (`${provider://reference}`).
 *
 * Implementations are discovered via [java.util.ServiceLoader]
 * or registered programmatically with [DescriptorPlaceholderResolver].
 * Each provider has a unique [id] (e.g. "kv", "aws-sm").
 */
interface SecretProvider {
    /** Unique provider identifier used in `${id://reference}` placeholders. */
    val id: String

    /**
     * Resolves a secret reference.
     * @param reference the opaque reference string after `://`
     * @return the resolved secret value
     * @throws SecretResolutionException if resolution fails
     */
    fun resolve(reference: String): String
}

/** Thrown when a [SecretProvider] cannot resolve a reference. */
class SecretResolutionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
