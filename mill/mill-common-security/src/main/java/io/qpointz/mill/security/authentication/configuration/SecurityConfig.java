package io.qpointz.mill.security.authentication.configuration;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
