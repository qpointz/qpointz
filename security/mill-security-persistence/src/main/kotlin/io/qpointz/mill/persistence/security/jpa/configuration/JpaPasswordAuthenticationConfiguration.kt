package io.qpointz.mill.persistence.security.jpa.configuration

import io.qpointz.mill.annotations.security.ConditionalOnSecurity
import io.qpointz.mill.persistence.security.jpa.auth.JpaUserRepo
import io.qpointz.mill.persistence.security.jpa.hasher.NoOpPasswordHasher
import io.qpointz.mill.persistence.security.jpa.repositories.GroupMembershipRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserCredentialRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserRepository
import io.qpointz.mill.security.authentication.AuthenticationMethod
import io.qpointz.mill.security.authentication.basic.BasicAuthenticationMethod
import io.qpointz.mill.security.authentication.basic.providers.UserRepoAuthenticationProvider
import io.qpointz.mill.security.domain.PasswordHasher
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Spring configuration that wires JPA-backed basic authentication into the security chain.
 *
 * This configuration is active only when security is enabled ([ConditionalOnSecurity]) and
 * the JPA identity infrastructure is present ([ConditionalOnBean] on [UserIdentityRepository]).
 * It provides:
 *
 * - A [PasswordEncoder] using [PasswordEncoderFactories.createDelegatingPasswordEncoder] for
 *   multi-algorithm verification via the `{prefix}` stored in each credential record.
 * - A [PasswordHasher] defaulting to [NoOpPasswordHasher] (`@ConditionalOnMissingBean`) for
 *   use when creating or updating credentials. Replace in production by declaring any other
 *   [PasswordHasher] bean.
 * - A [BasicAuthenticationMethod] backed by [UserRepoAuthenticationProvider] and [JpaUserRepo],
 *   at priority 299 (just below the file-store BASIC default of 300, ensuring JPA wins when
 *   both are active).
 */
@Configuration
@ConditionalOnSecurity
@ConditionalOnBean(UserIdentityRepository::class)
open class JpaPasswordAuthenticationConfiguration {

    /**
     * Provides a [PasswordEncoder] capable of verifying hashes produced by any algorithm
     * whose prefix is registered with Spring Security's delegating encoder.
     *
     * @return delegating [PasswordEncoder] dispatching on `{prefix}` in stored hash
     */
    @Bean
    open fun jpaPasswordEncoder(): PasswordEncoder =
        PasswordEncoderFactories.createDelegatingPasswordEncoder()

    /**
     * Provides the default [PasswordHasher] for credential creation.
     *
     * Uses [NoOpPasswordHasher] as a development-only fallback. Declare any other
     * [PasswordHasher] bean to replace this default without code changes.
     *
     * @return [NoOpPasswordHasher] instance
     */
    @Bean
    @ConditionalOnMissingBean(PasswordHasher::class)
    open fun passwordHasher(): PasswordHasher = NoOpPasswordHasher()

    /**
     * Provides the JPA-backed [BasicAuthenticationMethod].
     *
     * Builds a [JpaUserRepo] from the available repositories, wraps it in a
     * [UserRepoAuthenticationProvider], and exposes it as a [BasicAuthenticationMethod]
     * at priority 299.
     *
     * @param identityRepo repository for provider/subject → userId identity records
     * @param credentialRepo repository for password credential records
     * @param membershipRepo repository for group membership resolution
     * @param userRepo repository for canonical user records
     * @param passwordEncoder encoder used by [UserRepoAuthenticationProvider] for verification
     * @return [AuthenticationMethod] backed by JPA user resolution
     */
    @Bean
    open fun jpaBasicAuthMethod(
        identityRepo: UserIdentityRepository,
        credentialRepo: UserCredentialRepository,
        membershipRepo: GroupMembershipRepository,
        userRepo: UserRepository,
        passwordEncoder: PasswordEncoder,
    ): AuthenticationMethod {
        val jpaUserRepo = JpaUserRepo(identityRepo, credentialRepo, membershipRepo, userRepo)
        val provider = UserRepoAuthenticationProvider(jpaUserRepo, passwordEncoder)
        return BasicAuthenticationMethod(provider, 299)
    }
}
