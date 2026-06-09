package io.qpointz.mill.analysis.queries.it

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

/** Minimal API filter chain for secured REST integration tests (no auth providers). */
@Configuration
@EnableWebSecurity
class SecuredApiTestConfiguration {

    /**
     * @param http Spring Security builder
     * @return chain requiring authentication on API routes
     */
    @Bean
    @Order(1)
    fun securedApiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests { auth -> auth.anyRequest().authenticated() }
            .csrf { it.disable() }
            .cors { it.disable() }
        return http.build()
    }
}
