package io.qpointz.mill.security.configuration;

import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.oauth2.OAuth2ResourceServiceAuthenticationMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

@Slf4j
@Configuration
@ConditionalOnSecurity
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mill.security.authentication.oauth2-resource-server")
public class OAuth2AuthenticationConfiguration {

    @Getter
    @Setter
    private OAuth2ResourceServerProperties.Jwt jwt;

    @Bean
    AuthenticationMethod oauthResourceServerAuthenticationMethod() {
        if (jwt == null || jwt.getJwkSetUri()==null) {
            return null;
        }

        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwt.getJwkSetUri()).build();

        val provider = new JwtAuthenticationProvider(jwtDecoder);

        return new OAuth2ResourceServiceAuthenticationMethod(jwt, provider, 100);
    }

}
