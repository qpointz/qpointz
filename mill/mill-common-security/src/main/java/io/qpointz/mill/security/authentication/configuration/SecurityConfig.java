package io.qpointz.mill.security.authentication.configuration;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Optional;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mill.security")
public class SecurityConfig {

    @Getter
    @Setter
    private boolean enable;

    @Bean
    public AuthenticationMethods authenticationMethods(Optional<List<AuthenticationMethod>> providers) {
        return new AuthenticationMethods(providers.orElse(List.of()));
    }

    @Bean
    @Order
    @ConditionalOnProperty(name="mill.security.enable")
    SecurityFilterChain secureAll(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authHttp -> {
            authHttp.requestMatchers("**").authenticated();
        }).build();
    }

    @Bean
    @Order
    @ConditionalOnProperty(name="mill.security.enable", matchIfMissing = true, havingValue = "false")
    SecurityFilterChain allPermited(@Autowired(required = false) HttpSecurity http) throws Exception {
        if (http == null) {
            return null;
        }

        val chain =  http.authorizeHttpRequests(authHttp -> {
            authHttp.requestMatchers("**").permitAll();
        }).build();
        return chain;
    }

}
