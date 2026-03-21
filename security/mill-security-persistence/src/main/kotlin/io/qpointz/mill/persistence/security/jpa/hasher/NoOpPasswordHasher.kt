package io.qpointz.mill.persistence.security.jpa.hasher

import io.qpointz.mill.security.domain.PasswordHasher

/**
 * Development-only [PasswordHasher] implementation that stores passwords as plaintext
 * using Spring Security's `{noop}` prefix.
 *
 * **This implementation MUST NOT be used in production.** It stores passwords in plain
 * text, which provides no protection against credential theft. It is provided as the
 * autoconfigured default so that development and test environments work out of the box
 * without additional configuration.
 *
 * Replace in production by declaring any other [PasswordHasher] bean in the application
 * context — e.g. a BCrypt-backed implementation. The `@ConditionalOnMissingBean` on the
 * default bean in
 * [io.qpointz.mill.persistence.security.jpa.configuration.JpaPasswordAuthenticationConfiguration]
 * ensures this replacement happens automatically.
 */
class NoOpPasswordHasher : PasswordHasher {

    /**
     * Algorithm identifier — `"noop"` corresponds to Spring Security's
     * [org.springframework.security.crypto.password.NoOpPasswordEncoder].
     */
    override val algorithmId: String = "noop"

    /**
     * Wraps [plaintext] in the `{noop}` prefix required by
     * [org.springframework.security.crypto.factory.PasswordEncoderFactories].
     *
     * @param plaintext the raw password supplied by the user or admin
     * @return `{noop}<plaintext>` — e.g. `{noop}password`
     */
    override fun hash(plaintext: String): String = "{noop}$plaintext"
}
