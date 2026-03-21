package io.qpointz.mill.security.auth.configuration

import io.qpointz.mill.annotations.security.ConditionalOnSecurity
import io.qpointz.mill.security.authentication.AuthenticationMethods
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Security filter chain for protected auth endpoints (`/auth/me`, `/auth/logout`, `/auth/profile`).
 *
 * Provides two mutually exclusive beans at `@Order(-5)`:
 * - When security is **enabled**: `/auth/me` and `/auth/logout` are `permitAll` (controller
 *   returns `401` for unauthenticated callers); `/auth/profile` requires authentication.
 *   Authentication methods from [AuthenticationMethods] are applied so the session cookie
 *   is accepted on these paths.
 * - When security is **disabled**: all three paths are `permitAll` unconditionally.
 *
 * Runs before [AuthRoutesSecurityConfiguration] at `@Order(0)`.
 */
@Configuration
open class AuthSecuredSecurityConfiguration {

    /**
     * Secures `/auth/me`, `/auth/logout`, and `/auth/profile` when security is enabled.
     *
     * - `GET /auth/me` — `permitAll()` (controller returns `401` or anonymous response)
     * - `POST /auth/logout` — `permitAll()` (graceful no-op if already logged out)
     * - `PATCH /auth/profile` — `authenticated()` only
     *
     * @param http the [HttpSecurity] to configure
     * @param authenticationMethods registered authentication methods for session support
     * @return the built [SecurityFilterChain]
     */
    @Bean
    @Order(-5)
    @ConditionalOnSecurity
    open fun secureAuthPaths(
        http: HttpSecurity,
        authenticationMethods: AuthenticationMethods,
    ): SecurityFilterChain {
        http.securityMatcher("/auth/me", "/auth/logout", "/auth/profile")
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/auth/me", "/auth/logout").permitAll()
                auth.requestMatchers("/auth/profile").authenticated()
            }
            .csrf { it.disable() }
            .cors { it.disable() }

        authenticationMethods.getProviders().forEach { method ->
            method.applySecurityConfig(http)
        }

        return http.build()
    }

    /**
     * Permits all requests to `/auth/me`, `/auth/logout`, and `/auth/profile` when security is disabled.
     *
     * @param http the [HttpSecurity] to configure
     * @return the built [SecurityFilterChain]
     */
    @Bean
    @Order(-5)
    @ConditionalOnSecurity(false)
    open fun permitAuthPaths(http: HttpSecurity): SecurityFilterChain =
        http.securityMatcher("/auth/me", "/auth/logout", "/auth/profile")
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .csrf { it.disable() }
            .cors { it.disable() }
            .build()
}
