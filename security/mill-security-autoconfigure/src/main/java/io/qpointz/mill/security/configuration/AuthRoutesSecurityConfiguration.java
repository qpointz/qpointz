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
 * Security configuration for authentication-related routes.
 *
 * <p>Covers {@code /id/**}, {@code /oauth2/**}, {@code /login/**}, {@code /logout/**},
 * {@code /auth/**}, and {@code /error**}. These paths are always permitted so that
 * the login and OAuth2 redirect flows are accessible regardless of the overall security
 * posture. The active authentication methods are applied to wire the login mechanism.
 */
@Slf4j
@Configuration
public class AuthRoutesSecurityConfiguration {

    /**
     * Secures authentication routes when {@code mill.security.enable=true}.
     *
     * <p>Although all requests to the auth paths are permitted, the authentication method
     * configurations (login pages, OAuth2 flows, etc.) are applied here so that the
     * login infrastructure is properly registered.
     *
     * @param http                  the Spring {@link HttpSecurity} builder
     * @param authenticationMethods the active authentication methods to apply
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @ConditionalOnSecurity
    @Order(0)
    public SecurityFilterChain authRoutesSecureAll(HttpSecurity http,
                                                   AuthenticationMethods authenticationMethods) throws Exception {
        log.info("Securing `auth` routes access.");
        http
                .securityMatcher("/id/**", "/oauth2/**", "/login/**", "/logout/**", "/auth/**", "/error**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().permitAll()
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

}
