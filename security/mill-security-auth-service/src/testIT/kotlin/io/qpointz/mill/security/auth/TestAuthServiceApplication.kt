package io.qpointz.mill.security.auth

import io.qpointz.mill.security.authentication.AuthenticationMethod
import io.qpointz.mill.security.authentication.AuthenticationMethods
import io.qpointz.mill.security.authentication.basic.BasicAuthenticationMethod
import io.qpointz.mill.persistence.security.jpa.auth.JpaUserRepo
import io.qpointz.mill.persistence.security.jpa.hasher.NoOpPasswordHasher
import io.qpointz.mill.persistence.security.jpa.repositories.GroupMembershipRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserCredentialRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserRepository
import io.qpointz.mill.security.domain.PasswordHasher
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.factory.PasswordEncoderFactories

// @EnableJpaRepositories is intentionally absent — Spring Boot's JpaRepositoriesAutoConfiguration
// scans from the @SpringBootApplication package hierarchy. The SecurityJpaConfiguration
// @AutoConfiguration supplies @EnableJpaRepositories for the security JPA repositories.
@SpringBootApplication
@EntityScan(basePackages = ["io.qpointz.mill.persistence.security.jpa.entities"])
class TestAuthServiceApplication {

    /**
     * Test-only security configuration providing [AuthenticationMethods] and
     * [AuthenticationManager] for integration tests.
     *
     * Builds JPA-backed basic auth directly from repositories so we don't depend on
     * [io.qpointz.mill.security.configuration.SecurityConfig] (which is not an
     * autoconfiguration and is outside the component-scan root of this test application).
     */
    @Configuration
    @EnableWebSecurity
    open class TestSecurityConfig {

        /**
         * Provides the [AuthenticationMethods] bean required by [AuthSecuredSecurityConfiguration].
         *
         * @param identityRepo JPA identity repository
         * @param credentialRepo JPA credential repository
         * @param membershipRepo JPA group membership repository
         * @param userRepo JPA user repository
         * @return [AuthenticationMethods] backed by JPA basic auth
         */
        @Bean
        open fun authenticationMethods(
            identityRepo: UserIdentityRepository,
            credentialRepo: UserCredentialRepository,
            membershipRepo: GroupMembershipRepository,
            userRepo: UserRepository,
        ): AuthenticationMethods {
            val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
            val jpaUserRepo = JpaUserRepo(identityRepo, credentialRepo, membershipRepo, userRepo)
            val provider = io.qpointz.mill.security.authentication.basic.providers.UserRepoAuthenticationProvider(
                jpaUserRepo, encoder
            )
            val method: AuthenticationMethod = BasicAuthenticationMethod(provider, 1)
            return AuthenticationMethods(listOf(method))
        }

        /**
         * Provides the [AuthenticationManager] bean required by [AuthPublicController].
         *
         * @param authenticationMethods registered authentication methods
         * @return [AuthenticationManager] backed by JPA user resolution
         */
        @Bean
        open fun authenticationManager(authenticationMethods: AuthenticationMethods): AuthenticationManager {
            val providers = authenticationMethods.providers.map { it.authenticationProvider }
            return ProviderManager(providers)
        }

        /**
         * Provides a [PasswordHasher] bean for the registration endpoint in integration tests.
         *
         * Uses [NoOpPasswordHasher] which is acceptable in the test environment. In production,
         * [io.qpointz.mill.persistence.security.jpa.configuration.JpaPasswordAuthenticationConfiguration]
         * provides this bean via its `@ConditionalOnMissingBean`-guarded factory.
         */
        @Bean
        open fun testPasswordHasher(): PasswordHasher = NoOpPasswordHasher()
    }
}
