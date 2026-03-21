package io.qpointz.mill.security.configuration;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for {@code /api/**} routes.
 *
 * <p>When security is enabled ({@code mill.security.enable=true}) all requests to
 * {@code /api/**} must be authenticated. When security is disabled the same paths are
 * open to anonymous access. CSRF and CORS protection are disabled for the API surface
 * because API consumers are expected to authenticate via bearer tokens or HTTP Basic.
 */
@Slf4j
@Configuration
public class ApiSecurityConfiguration {

    /**
     * Secures all {@code /api/**} requests when {@code mill.security.enable=true}.
     *
     * @param http                  the Spring {@link HttpSecurity} builder
     * @param authenticationMethods the active authentication methods to apply
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @Order(1)
    @ConditionalOnSecurity
    public SecurityFilterChain secureApiRequests(HttpSecurity http,
                                                 AuthenticationMethods authenticationMethods) throws Exception {
        log.debug("Security API requests.");
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable);

        authenticationMethods.getProviders().forEach(m -> {
            try {
                m.applySecurityConfig(http);
            } catch (Exception ex) {
                throw new MillRuntimeException(ex);
            }
        });
        return http.build();
    }

    /**
     * Permits all {@code /api/**} requests when {@code mill.security.enable=false}.
     *
     * @param http the Spring {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @Order(1)
    @ConditionalOnSecurity(false)
    public SecurityFilterChain permitApiRequests(HttpSecurity http) throws Exception {
        log.debug("Permit all API requests.");
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
