package io.qpointz.mill.service.security;

import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NoopAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        val username = authentication.getName().toLowerCase();
        val password = authentication.getCredentials().toString();
        return new UsernamePasswordAuthenticationToken(username, password, List.of());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
