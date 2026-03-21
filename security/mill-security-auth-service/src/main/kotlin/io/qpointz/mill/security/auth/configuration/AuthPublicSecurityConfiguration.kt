package io.qpointz.mill.security.auth.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Security filter chain for public auth endpoints under `/auth/public/`.
 *
 * Always permits all requests to the `/auth/public/` path prefix regardless of whether
 * security is enabled. This chain runs at [ORDER], before `AuthRoutesSecurityConfiguration`
 * at `@Order(0)`, ensuring that login and registration endpoints are always reachable
 * without a prior session.
 *
 * CSRF and CORS are disabled for these JSON API endpoints.
 */
@Configuration
open class AuthPublicSecurityConfiguration {

    companion object {
        /** Spring Security order value for this filter chain — fires before the default auth chains. */
        const val ORDER = -6

        /** Ant pattern matching all public auth endpoints. */
        const val AUTH_PUBLIC_PATTERN = "/auth/public/**"
    }

    /**
     * Permits all requests to the public auth path prefix unconditionally.
     *
     * Handles `POST /auth/public/login` and any future public auth endpoints
     * such as registration or forgot-password flows. No authentication required.
     *
     * @param http the [HttpSecurity] to configure
     * @return the built [SecurityFilterChain]
     */
    @Bean
    @Order(ORDER)
    open fun permitAuthPublicPaths(http: HttpSecurity): SecurityFilterChain =
        http.securityMatcher(AUTH_PUBLIC_PATTERN)
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .csrf { it.disable() }
            .cors { it.disable() }
            .build()
}
