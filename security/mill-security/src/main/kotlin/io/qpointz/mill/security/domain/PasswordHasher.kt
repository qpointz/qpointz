package io.qpointz.mill.security.domain

/**
 * Strategy for producing password hashes when creating or updating credentials.
 *
 * The [hash] method returns a Spring-Security-compatible encoded string in `{prefix}encoded`
 * format (e.g. `{noop}password` or `{bcrypt}$2a$10$...`). The prefix is consumed by
 * [org.springframework.security.crypto.factory.PasswordEncoderFactories] on the verification
 * side — no custom verification logic is required here.
 *
 * The active implementation is injected as a `@Bean`. Production deployments replace the
 * autoconfigured default ([algorithmId] = `"noop"`) by declaring a stronger [PasswordHasher]
 * bean in the application context.
 */
interface PasswordHasher {

    /**
     * Short identifier stored in `UserCredentialRecord.algorithm` for audit and rotation purposes.
     *
     * Examples: `"noop"`, `"bcrypt"`, `"argon2"`.
     */
    val algorithmId: String

    /**
     * Produces the encoded credential string to be stored in `UserCredentialRecord.passwordHash`.
     *
     * @param plaintext the raw password supplied by the user or admin
     * @return Spring-Security-compatible `{prefix}encoded` string (e.g. `{noop}password`)
     */
    fun hash(plaintext: String): String
}
