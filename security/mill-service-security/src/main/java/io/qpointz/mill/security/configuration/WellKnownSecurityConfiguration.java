package io.qpointz.mill.security.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@ConditionalOnMissingBean(name = "functionContextFlag") //TODO: hack to figure out if it running as function
public class WellKnownSecurityConfiguration {

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
