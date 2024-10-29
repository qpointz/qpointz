package io.qpointz.mill.security.authentication.configuration;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

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
    @ConditionalOnProperty(name="mill.security.enable")
    SecurityFilterChain secureAll(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authHttp -> {
            authHttp.requestMatchers("**").authenticated();
        }).build();
    }

    @Bean
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
