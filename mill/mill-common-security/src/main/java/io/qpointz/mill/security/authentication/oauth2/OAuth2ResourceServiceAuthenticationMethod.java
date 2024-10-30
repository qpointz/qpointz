package io.qpointz.mill.security.authentication.oauth2;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@AllArgsConstructor
public class OAuth2ResourceServiceAuthenticationMethod implements AuthenticationMethod {

    @Getter
    private OAuth2ResourceServerProperties.Jwt jwt;

    @Getter
    public AuthenticationProvider authenticationProvider;

    @Getter
    public final int methodPriority;

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.OAUTH2;
    }

    @Override
    public void applyDefaultHttpSecurity(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(ht ->ht.jwt(jwc-> jwc.jwkSetUri(jwt.getJwkSetUri())));
    }
}
