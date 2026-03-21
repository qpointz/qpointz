package io.qpointz.mill.security.configuration;

import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.util.List;
import java.util.Optional;

/**
 * Root Spring Security configuration for the Mill service.
 *
 * <p>This class is the single entry point that carries {@link EnableWebSecurity},
 * activating the entire Spring Security filter chain. It also binds the top-level
 * {@code mill.security.*} configuration properties and produces the
 * {@link AuthenticationMethods} and {@link AuthenticationManager} beans that are
 * consumed by the individual route-level filter chain configurations.
 *
 * <p>The {@code mill.security.enable} property controls whether security is active;
 * individual {@link ConditionalOnSecurity}-gated beans react to this flag.
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mill.security")
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    /**
     * Whether Mill security is enabled.
     *
     * <p>Set {@code mill.security.enable=true} to activate authentication and
     * authorization across all route-level filter chains. Defaults to {@code false}.
     */
    @Getter
    @Setter
    private boolean enable;

    /**
     * Creates a new {@code SecurityConfig} instance.
     *
     * <p>Logs a debug message confirming that the security auto-configuration has been
     * initialized. This constructor is called by Spring during context startup.
     */
    public SecurityConfig() {
        log.debug("Default security configuration initialized");
    }

    /**
     * Builds the {@link AuthenticationMethods} aggregate from all registered
     * {@link AuthenticationMethod} beans.
     *
     * <p>If no {@link AuthenticationMethod} beans are present in the context (e.g. when
     * security is disabled or no authentication provider is configured) an empty
     * {@link AuthenticationMethods} is returned so downstream beans have a safe default.
     *
     * @param providers the optional list of {@link AuthenticationMethod} beans discovered
     *                  by Spring
     * @return the assembled {@link AuthenticationMethods}
     */
    @Bean
    public AuthenticationMethods authenticationMethods(Optional<List<? extends AuthenticationMethod>> providers) {
        log.debug("Build Authentication methods");
        return new AuthenticationMethods(providers.orElse(List.of()));
    }

    /**
     * Collects all {@link AuthenticationProvider} instances from the registered
     * authentication methods.
     *
     * <p>Only active when {@code mill.security.enable=true}.
     *
     * @param authenticationMethods the aggregate of active authentication methods
     * @return the list of {@link AuthenticationProvider} instances to be registered with
     *         the {@link AuthenticationManager}
     */
    @Bean
    @ConditionalOnSecurity
    public List<AuthenticationProvider> authenticationProviders(AuthenticationMethods authenticationMethods) {
        return authenticationMethods.getProviders().stream()
                .map(AuthenticationMethod::getAuthenticationProvider)
                .toList();
    }

    /**
     * Creates the {@link AuthenticationManager} backed by all registered
     * {@link AuthenticationProvider} instances.
     *
     * <p>Only active when {@code mill.security.enable=true}.
     *
     * @param http      the Spring {@link HttpSecurity} builder (used to share the
     *                  {@link AuthenticationManager} with the filter chain)
     * @param providers the list of {@link AuthenticationProvider} instances
     * @return the configured {@link AuthenticationManager}
     * @throws Exception if the manager cannot be constructed
     */
    @Bean
    @ConditionalOnSecurity
    public AuthenticationManager authenticationProviderManager(HttpSecurity http,
                                                               List<AuthenticationProvider> providers) throws Exception {
        if (log.isInfoEnabled()) {
            providers.forEach(k -> log.info("Registered auth provider:{}", k.getClass().getCanonicalName()));
        }
        return new ProviderManager(providers);
    }

}
