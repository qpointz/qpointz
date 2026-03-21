package io.qpointz.mill.security.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for {@code /.well-known/**} routes.
 *
 * <p>Discovery endpoints under {@code /.well-known/} are always open to anonymous
 * access regardless of the overall security posture. This is required for OIDC discovery,
 * JWKS endpoints, and similar standards-based discovery mechanisms.
 */
@Slf4j
@Configuration
public class WellKnownSecurityConfiguration {

    /**
     * Permits all {@code /.well-known/**} requests unconditionally.
     *
     * @param http the Spring {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @Order(0)
    SecurityFilterChain permitWellKnownAccess(HttpSecurity http) throws Exception {
        log.info("Securing `.well-known` routes access.");
        return http.securityMatcher("/.well-known/**")
                .authorizeHttpRequests(a ->
                        a.anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .build();
    }

}
