package io.qpointz.mill.app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Allows unauthenticated HTTP GET to curated Actuator inspection endpoints used for local
 * diagnostics (value-mapping wiring, optional full {@code /beans}). Lock down in production
 * (network ACL, management port, or authenticated access) if these endpoints are exposed.
 */
@Configuration
public class MillServiceActuatorInspectSecurityConfiguration {

    /**
     * Permits read access to {@code /actuator/valuemap} and, when exposed, {@code /actuator/beans}.
     *
     * @param http the Spring {@link HttpSecurity} builder
     * @return a filter chain with higher precedence than application API security
     * @throws Exception if the chain cannot be built
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain actuatorInspectPermitAll(HttpSecurity http) throws Exception {
        http.securityMatcher("/actuator/valuemap", "/actuator/valuemap/**", "/actuator/beans", "/actuator/beans/**");
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
