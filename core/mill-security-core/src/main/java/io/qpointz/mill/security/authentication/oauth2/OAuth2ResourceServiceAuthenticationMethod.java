package io.qpointz.mill.security.authentication.oauth2;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationMethodDescriptor;
import io.qpointz.mill.security.authentication.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class OAuth2ResourceServiceAuthenticationMethod implements AuthenticationMethod {

    @Getter
    private OAuth2ResourceServerProperties.Jwt jwt;

    @Getter
    public AuthenticationProvider authenticationProvider;

    @Getter
    public final int methodPriority;

    @Getter
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.OAUTH2;
    }

    @Override
    public void applyLoginConfig(HttpSecurity http) throws Exception {

        if (clientRegistrationRepository == null) {
            log.warn("OAuth2 authentication method enabled , but no client registration provided. Skipping configuration");
            return;
        }
        //this is subject for refactoring !!!!
        http.oauth2Login(oauth -> oauth
            .loginPage("/id/login.html")
            .permitAll());
    }

    @Override
    public void applySecurityConfig(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(ht ->ht
                .jwt(jwc->
                        jwc.jwkSetUri(jwt.getJwkSetUri())));
    }



    @Override
    public AuthenticationMethodDescriptor getDescriptor() {
        val providers = new ArrayList<String>();

        if (clientRegistrationRepository == null) {
            return new OAuth2ResourceServiceAuthenticationMethodDescriptor(providers);
        }

        if (clientRegistrationRepository instanceof Iterable) {
            for (val registration : (Iterable<ClientRegistration>) clientRegistrationRepository) {
                providers.add(registration.getRegistrationId());
            }
        }
        return new OAuth2ResourceServiceAuthenticationMethodDescriptor(providers);
    }

    public record OAuth2ResourceServiceAuthenticationMethodDescriptor(List<String> names) implements AuthenticationMethodDescriptor {

        @Override
        public AuthenticationType getAuthenticationType() {
            return AuthenticationType.OAUTH2;
        }
    }
}
