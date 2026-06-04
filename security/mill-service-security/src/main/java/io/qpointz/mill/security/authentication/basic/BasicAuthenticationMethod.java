package io.qpointz.mill.security.authentication.basic;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Slf4j
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
        log.info("Enabling HTTP Basic authentication on filter chain");
        http.httpBasic(Customizer.withDefaults());
    }

    @Override
    public void applySecurityConfig(HttpSecurity http) throws Exception {

    }

}
