package io.qpointz.mill.security.authentication.oauth2;

import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

/**
 * Auto-configuration for OAuth2 resource-server (JWT bearer token) authentication.
 *
 * <p>Binds to the {@code mill.security.authentication.oauth2-resource-server.*}
 * configuration prefix and is only active when {@code mill.security.enable=true}.
 * When {@code mill.security.authentication.oauth2-resource-server.enable=true} a
 * {@link JwtAuthenticationProvider}-backed {@link AuthenticationMethod} is registered
 * using the JWK Set URI supplied in the JWT configuration.
 */
@Slf4j
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mill.security.authentication.oauth2-resource-server")
@Configuration
@ConditionalOnSecurity
public class OAuth2AuthenticationConfiguration {

    /**
     * JWT configuration for the OAuth2 resource server, bound from
     * {@code mill.security.authentication.oauth2-resource-server.jwt.*}.
     */
    @Getter
    @Setter
    private OAuth2ResourceServerProperties.Jwt jwt;

    /**
     * Creates the OAuth2 resource-server {@link AuthenticationMethod} when
     * {@code mill.security.authentication.oauth2-resource-server.enable=true}.
     *
     * <p>If no valid JWT configuration (JWK Set URI) is available the method logs a
     * warning and returns {@code null} so that Spring silently skips this provider.
     *
     * @param clientRegistrationRepository the optional OIDC client registration
     *                                     repository, injected when available
     * @return the configured {@link AuthenticationMethod}, or {@code null} if the JWT
     *         configuration is incomplete
     */
    @Bean
    @ConditionalOnProperty(prefix = "mill.security.authentication.oauth2-resource-server", name = "enable")
    public AuthenticationMethod oauthResourceServerAuthenticationMethod(
            @Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository) {
        if (jwt == null || jwt.getJwkSetUri() == null) {
            log.warn("No valid JWT configuration provided. OAuth resource service method will not be used");
            return null;
        }

        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwt.getJwkSetUri()).build();
        val provider = new JwtAuthenticationProvider(jwtDecoder);

        return new OAuth2ResourceServiceAuthenticationMethod(jwt, provider, 100, clientRegistrationRepository);
    }

}
