package io.qpointz.mill.security.authentication;

import org.springframework.security.authentication.AuthenticationProvider;

public interface AuthenticationMethod {

    AuthenticationProvider getAuthenticationProvider();

    int getMethodPriority();

    AuthenticationType getAuthenticationType();

}
