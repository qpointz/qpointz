package io.qpointz.mill.security.configuration;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import io.qpointz.mill.security.annotations.ConditionalOnSecurity;

import java.util.List;
import java.util.Optional;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mill.security")
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Getter
    @Setter
    private boolean enable;

    @Bean
    public AuthenticationMethods authenticationMethods(Optional<List<AuthenticationMethod>> providers) {
        return new AuthenticationMethods(providers.orElse(List.of()));
    }

    @Bean
    @ConditionalOnSecurity
    public List<AuthenticationProvider> authenticationProviders(AuthenticationMethods authenticationMethods) {
        return authenticationMethods.getProviders().stream()
                .map(AuthenticationMethod::getAuthenticationProvider)
                .toList();
    }

    @Bean
    @ConditionalOnSecurity
    public AuthenticationManager authenticationProviderManager(HttpSecurity http, List<AuthenticationProvider> providers) throws Exception {
        if (log.isInfoEnabled()) {
            providers.stream().forEach(k-> log.info("Registered auth provider:{}",k.getClass().getCanonicalName()));
        }
        return new ProviderManager(providers);
    }




    @Bean
    @ConditionalOnSecurity
    @Order
    @ConditionalOnMissingBean(name = "functionContextFlag") //TODO: hack to figure out if it running as function
    SecurityFilterChain secureAll(HttpSecurity http ,
                                  AuthenticationManager authenticationManager,
                                  AuthenticationMethods authenticationMethods) throws Exception {

        http.authenticationManager(authenticationManager)
                .securityMatcher("/**")
                .authorizeHttpRequests(authHttp ->  authHttp
                    .anyRequest().authenticated()
                );

        for (var m: authenticationMethods.getProviders()) {
            m.applyDefaultHttpSecurity(http);
        }

        return http.build();
    }

    @Bean
    @ConditionalOnSecurity(false)
    SecurityFilterChain allPermited(@Autowired(required = false) HttpSecurity http) throws Exception {
        if (http == null) {
            return null;
        }
        return   http.authorizeHttpRequests(authHttp -> authHttp
                .requestMatchers("/**").permitAll()
        ).build();
    }

}
