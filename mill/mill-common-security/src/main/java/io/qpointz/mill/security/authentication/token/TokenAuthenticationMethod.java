package io.qpointz.mill.security.authentication.token;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.authentication.AuthenticationProvider;

@AllArgsConstructor
public class TokenAuthenticationMethod implements AuthenticationMethod {

    @Getter
    private final AuthenticationProvider authenticationProvider;

    @Getter
    private final int methodPriority;


    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.BEARER_TOKEN;
    }
}
