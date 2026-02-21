package io.qpointz.mill.security.authentication.basic;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationMethodDescriptor;
import io.qpointz.mill.security.authentication.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@AllArgsConstructor
public class BasicAuthenticationMethod implements AuthenticationMethod {

    @Getter
    private final AuthenticationProvider authenticationProvider;

    @Getter
    private final int methodPriority;

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.BASIC;
    }

    @Override
    public void applyLoginConfig(HttpSecurity http) throws Exception {
        //no specific configuration required
        //potentially to be extended with formLogin
        http.httpBasic(Customizer.withDefaults());
    }

    @Override
    public void applySecurityConfig(HttpSecurity http) throws Exception {

    }


    @Override
    public AuthenticationMethodDescriptor getDescriptor() {
        return new BasicAuthenticationMethodDescriptor();
    }

    public static class BasicAuthenticationMethodDescriptor implements AuthenticationMethodDescriptor {

        @Override
        public AuthenticationType getAuthenticationType() {
            return AuthenticationType.BASIC;
        }
    }


}
