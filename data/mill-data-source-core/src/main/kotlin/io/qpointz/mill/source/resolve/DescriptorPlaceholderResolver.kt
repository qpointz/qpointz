package io.qpointz.mill.source.resolve

/**
 * Single choke point for placeholder substitution in descriptor string fields.
 *
 * Supported grammar (frozen):
 * - `${ID}` — resolves via environment variables / system properties
 * - `${env://ID}` — equivalent to `${ID}` (explicit env channel)
 * - `${provider://reference}` — dispatches to a registered [SecretProvider]
 *
 * Unknown providers or failed lookups produce errors (fail closed);
 * no silent fallback from provider-qualified tokens to env.
 */
class DescriptorPlaceholderResolver(
    providers: List<SecretProvider> = emptyList(),
    private val envLookup: (String) -> String? = { key -> System.getenv(key) }
) {
    private val providerMap: Map<String, SecretProvider> = providers.associateBy { it.id }

    private companion object {
        val PLACEHOLDER_PATTERN = Regex("""\$\{([^}]+)}""")
        val PROVIDER_PATTERN = Regex("""([a-zA-Z][\w-]*)://(.+)""")
    }

    /**
     * Resolves all placeholders in the given [input] string.
     * @return the resolved string with all `${...}` tokens substituted
     * @throws PlaceholderResolutionException if any token cannot be resolved
     */
    fun resolve(input: String): String {
        return PLACEHOLDER_PATTERN.replace(input) { match ->
            val content = match.groupValues[1]
            resolveToken(content)
        }
    }

    /**
     * Resolves placeholders in all string values of the given map (shallow).
     * @param values map whose string values may contain placeholders
     * @return a new map with all placeholders resolved; null values pass through
     */
    fun resolveMap(values: Map<String, String?>): Map<String, String?> {
        return values.mapValues { (_, v) -> v?.let { resolve(it) } }
    }

    private fun resolveToken(content: String): String {
        val providerMatch = PROVIDER_PATTERN.matchEntire(content)
        if (providerMatch != null) {
            val providerId = providerMatch.groupValues[1]
            val reference = providerMatch.groupValues[2]

            if (providerId == "env") {
                return resolveEnv(reference)
            }

            val provider = providerMap[providerId]
                ?: throw PlaceholderResolutionException("Unknown secret provider: '$providerId'")
            try {
                return provider.resolve(reference)
            } catch (e: SecretResolutionException) {
                throw PlaceholderResolutionException(
                    "Provider '$providerId' failed to resolve '$reference'", e
                )
            }
        }

        return resolveEnv(content)
    }

    private fun resolveEnv(key: String): String {
        return envLookup(key)
            ?: throw PlaceholderResolutionException("Environment variable '$key' is not set")
    }
}

/** Thrown when placeholder resolution fails. */
class PlaceholderResolutionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
