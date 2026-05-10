package io.qpointz.mill.security.configuration;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for {@code /app/**} routes.
 *
 * <p>When security is enabled ({@code mill.security.enable=true}) all requests to
 * {@code /app/**} must be authenticated and the active login flow is applied. When
 * security is disabled the same paths are open to anonymous access.
 */
@Slf4j
@Configuration
public class AppSecurityConfiguration {

    /**
     * Secures all {@code /app/**} requests when {@code mill.security.enable=true}.
     *
     * <p>Public SPA entry points ({@code /app/login}, {@code /app/register}) and static assets
     * under {@code /app/assets/**} must stay anonymous; otherwise OAuth2 {@code loginPage("/app/login")}
     * and the packaged UI shell would cause an infinite redirect loop. Root-level icons referenced
     * from {@code index.html} (e.g. {@code /app/favicon-512x512.png}) must also be anonymous so a
     * browser prefetch is not stored as the post-login {@code SavedRequest}.
     *
     * @param http                  the Spring {@link HttpSecurity} builder
     * @param authenticationMethods the active authentication methods to apply
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @Order(1)
    @ConditionalOnSecurity
    public SecurityFilterChain secureAppRequests(HttpSecurity http,
                                                 AuthenticationMethods authenticationMethods) throws Exception {
        log.info("Securing `app` routes access.");
        http
                .securityMatcher("/app/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .requestMatchers(
                                "/app",
                                "/app/",
                                "/app/index.html",
                                "/app/assets/**",
                                "/app/*.png",
                                "/app/*.ico",
                                "/app/*.svg",
                                "/app/*.webmanifest",
                                "/app/login",
                                "/app/login/**",
                                "/app/register",
                                "/app/register/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        authenticationMethods.getProviders().forEach(m -> {
            try {
                m.applyLoginConfig(http);
                m.applySecurityConfig(http);
            } catch (Exception ex) {
                throw new MillRuntimeException(ex);
            }
        });
        return http.build();
    }

    /**
     * Permits all {@code /app/**} requests when {@code mill.security.enable=false}.
     *
     * @param http the Spring {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @Order(1)
    @ConditionalOnSecurity(false)
    public SecurityFilterChain permitAppRequests(HttpSecurity http) throws Exception {
        log.trace("Permit any `app` route.");
        http
                .securityMatcher("/app/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
