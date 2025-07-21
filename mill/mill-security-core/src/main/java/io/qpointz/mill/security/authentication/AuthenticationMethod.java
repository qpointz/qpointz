package io.qpointz.mill.security.authentication;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface AuthenticationMethod {

    AuthenticationProvider getAuthenticationProvider();

    int getMethodPriority();

    AuthenticationType getAuthenticationType();

    void applyLoginConfig(HttpSecurity http) throws Exception;

    void applySecurityConfig(HttpSecurity http) throws Exception;

    AuthenticationMethodDescriptor getDescriptor();
}
