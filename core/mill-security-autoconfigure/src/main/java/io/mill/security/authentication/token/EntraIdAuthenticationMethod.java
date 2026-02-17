package io.qpointz.mill.security.authentication.token;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationMethodDescriptor;
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
    public void applyLoginConfig(HttpSecurity http) throws Exception {
        //no specific configuration required
    }

    @Override
    public void applySecurityConfig(HttpSecurity http) throws Exception {
        //no specific configuration required
    }

    @Override
    public AuthenticationMethodDescriptor getDescriptor() {
        return null;
    }

    public static class EntraIdAuthenticationMethodDescriptor implements AuthenticationMethodDescriptor {
        @Override
        public AuthenticationType getAuthenticationType() {
            return AuthenticationType.CUSTOM;
        }
    }
}
