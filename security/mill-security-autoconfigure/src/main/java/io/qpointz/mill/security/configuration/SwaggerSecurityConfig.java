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
 * Security configuration for Swagger / OpenAPI UI routes.
 *
 * <p>Covers {@code /swagger-ui.html}, {@code /swagger-ui/**}, and {@code /v3/api-docs/**}.
 * When security is enabled ({@code mill.security.enable=true}) these paths require
 * authentication. When security is disabled they are open to anonymous access.
 */
@Slf4j
@Configuration
public class SwaggerSecurityConfig {

    /**
     * Secures Swagger UI routes when {@code mill.security.enable=true}.
     *
     * @param http                  the Spring {@link HttpSecurity} builder
     * @param authenticationMethods the active authentication methods to apply
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @ConditionalOnSecurity
    @Order(1)
    public SecurityFilterChain secureSwaggerRequests(HttpSecurity http,
                                                     AuthenticationMethods authenticationMethods) throws Exception {
        log.info("Securing `swagger` routes access.");
        http
                .securityMatcher("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                .authorizeHttpRequests(authHttp -> authHttp
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
     * Permits all Swagger UI routes when {@code mill.security.enable=false}.
     *
     * @param http                  the Spring {@link HttpSecurity} builder
     * @param authenticationMethods the active authentication methods (unused but kept for
     *                              symmetry with the secure variant)
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @Order(1)
    @ConditionalOnSecurity(false)
    public SecurityFilterChain permitSwaggerRequests(HttpSecurity http,
                                                     AuthenticationMethods authenticationMethods) throws Exception {
        log.trace("Permit any `swagger` route.");
        http
                .securityMatcher("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().permitAll()
                );

        return http.build();
    }

}
