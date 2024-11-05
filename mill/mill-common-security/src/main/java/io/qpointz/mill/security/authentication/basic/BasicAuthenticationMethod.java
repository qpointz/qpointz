package io.qpointz.mill.security.authentication.basic;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
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
    public void applyDefaultHttpSecurity(HttpSecurity http) throws Exception {
       http.httpBasic(Customizer.withDefaults());
    }


}
