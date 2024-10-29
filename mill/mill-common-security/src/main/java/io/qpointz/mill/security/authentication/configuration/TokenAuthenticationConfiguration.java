package io.qpointz.mill.security.authentication.configuration;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.token.DefaultJwtDecoder;
import io.qpointz.mill.security.authentication.token.TokenAuthenticationMethod;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

@Configuration
@ConditionalOnProperty(name="mill.security.enable")
public class TokenAuthenticationConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "mill.security.authentication.token.jwt", name = "jwk-set-uri")
    public JwtDecoder jwtDecoder(@Value("${mill.security.authentication.token.jwt.jwk-set-uri}") String jwkSetUri) {
        return new DefaultJwtDecoder(jwkSetUri);
    }

    @Bean
    @ConditionalOnBean(JwtDecoder.class)
    public AuthenticationMethod jwtAuthenticationMethod(JwtDecoder jwtDecoder) {
        val provider = new JwtAuthenticationProvider(jwtDecoder);
        return new TokenAuthenticationMethod(provider, 100);
    }


}
