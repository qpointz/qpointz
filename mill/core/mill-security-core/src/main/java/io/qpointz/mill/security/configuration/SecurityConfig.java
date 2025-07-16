package io.qpointz.mill.security.configuration;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mill.security")
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Getter
    @Setter
    private boolean enable;

    public SecurityConfig() {
        log.debug("Default security configuration initialized");
    }

    @Bean
    public AuthenticationMethods authenticationMethods(Optional<List<? extends AuthenticationMethod>> providers) {
        log.debug("Build Authentication methods");
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
    
}
