package io.qpointz.mill.source.resolve

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DescriptorPlaceholderResolverTest {

    private val env = mapOf(
        "DB_HOST" to "localhost",
        "DB_PORT" to "5432",
        "SECRET_KEY" to "s3cret"
    )

    private val stubProvider = object : SecretProvider {
        override val id = "vault"
        override fun resolve(reference: String): String = when (reference) {
            "db/password" -> "vault-password-123"
            else -> throw SecretResolutionException("Not found: $reference")
        }
    }

    private fun resolver(
        providers: List<SecretProvider> = listOf(stubProvider)
    ) = DescriptorPlaceholderResolver(providers) { env[it] }

    @Test
    fun shouldResolveSimpleEnvVar() {
        assertEquals("localhost", resolver().resolve("\${DB_HOST}"))
    }

    @Test
    fun shouldResolveExplicitEnvPrefix() {
        assertEquals("localhost", resolver().resolve("\${env://DB_HOST}"))
    }

    @Test
    fun shouldDispatchToRegisteredProvider() {
        assertEquals("vault-password-123", resolver().resolve("\${vault://db/password}"))
    }

    @Test
    fun shouldThrowOnUnknownProvider() {
        assertThrows<PlaceholderResolutionException> {
            resolver().resolve("\${unknown://some-ref}")
        }
    }

    @Test
    fun shouldThrowOnMissingEnvVar() {
        assertThrows<PlaceholderResolutionException> {
            resolver().resolve("\${MISSING_VAR}")
        }
    }

    @Test
    fun shouldPassThroughLiterals() {
        assertEquals("no-placeholders-here", resolver().resolve("no-placeholders-here"))
    }

    @Test
    fun shouldResolveMultiplePlaceholders() {
        val result = resolver().resolve("jdbc:postgresql://\${DB_HOST}:\${DB_PORT}/mydb")
        assertEquals("jdbc:postgresql://localhost:5432/mydb", result)
    }

    @Test
    fun shouldBeIdempotentOnLiteralValues() {
        val literal = "already-resolved-value"
        val first = resolver().resolve(literal)
        val second = resolver().resolve(first)
        assertEquals(literal, second)
    }

    @Test
    fun shouldResolveMixedEnvAndProviderPlaceholders() {
        val result = resolver().resolve("host=\${DB_HOST} pass=\${vault://db/password}")
        assertEquals("host=localhost pass=vault-password-123", result)
    }

    @Test
    fun shouldThrowWhenProviderFails() {
        assertThrows<PlaceholderResolutionException> {
            resolver().resolve("\${vault://nonexistent/path}")
        }
    }

    @Test
    fun shouldResolveMapValues() {
        val input = mapOf(
            "host" to "\${DB_HOST}",
            "port" to "\${DB_PORT}",
            "literal" to "plain-value",
            "nullable" to null
        )
        val result = resolver().resolveMap(input)
        assertEquals("localhost", result["host"])
        assertEquals("5432", result["port"])
        assertEquals("plain-value", result["literal"])
        assertNull(result["nullable"])
    }

    @Test
    fun shouldWorkWithNoProviders() {
        val resolver = DescriptorPlaceholderResolver(emptyList()) { env[it] }
        assertEquals("localhost", resolver.resolve("\${DB_HOST}"))
    }
}
