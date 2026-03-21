package io.qpointz.mill.security.configuration;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for {@code /services/**} routes.
 *
 * <p>When security is enabled ({@code mill.security.enable=true}) all requests to
 * {@code /services/**} must be authenticated. CSRF and CORS protection are disabled
 * because these endpoints are consumed by service-to-service clients that authenticate
 * via bearer tokens. When security is disabled the same paths are open to anonymous access.
 */
@Slf4j
@Configuration
public class ServicesSecurityConfiguration {

    /**
     * Secures all {@code /services/**} requests when {@code mill.security.enable=true}.
     *
     * @param http                  the Spring {@link HttpSecurity} builder
     * @param authenticationMethods the active authentication methods to apply
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @ConditionalOnSecurity
    public SecurityFilterChain secureServicesRequests(HttpSecurity http,
                                                      AuthenticationMethods authenticationMethods) throws Exception {
        log.info("Securing `services` routes access.");
        http
                .securityMatcher("/services/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable);

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
     * Permits all {@code /services/**} requests when {@code mill.security.enable=false}.
     *
     * @param http                  the Spring {@link HttpSecurity} builder
     * @param authenticationMethods the active authentication methods (unused but kept for
     *                              symmetry with the secure variant)
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    @ConditionalOnSecurity(false)
    public SecurityFilterChain permitServicesRequests(HttpSecurity http,
                                                      AuthenticationMethods authenticationMethods) throws Exception {
        log.debug("Permit all services requests.");
        http
                .securityMatcher("/services/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
