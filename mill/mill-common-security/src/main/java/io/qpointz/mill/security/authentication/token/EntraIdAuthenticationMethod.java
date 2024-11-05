package io.qpointz.mill.security.authentication.token;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@AllArgsConstructor
public class EntraIdAuthenticationMethod implements AuthenticationMethod {

    private final AuthenticationProvider authenticationProvider = new EntraIdTokenAuthenticationProvider();

    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return this.authenticationProvider;
    }

    @Getter
    private int methodPriority;

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.OAUTH2;
    }

    @Override
    public void applyDefaultHttpSecurity(HttpSecurity http) throws Exception {
        //no specific configuration need to be applied
    }
}
