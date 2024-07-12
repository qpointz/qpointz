package io.qpointz.mill.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Set;

@Slf4j
public class DenySecurityProviderFactory implements SecurityProviderFactory<DenySecurityProviderFactory.DenyAuthenticationProvider> {

    @Slf4j
    public static class DenyAuthenticationProvider implements AuthenticationProvider {

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            log.info("Authentication rejected by deny authentication provider");
            return null;
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return true;
        }
    }

    private static final DenyAuthenticationProvider DEFAULT_PROVIDER = new DenyAuthenticationProvider();

    @Override
    public DenyAuthenticationProvider createAuthenticationProvider(Map<String, Object> config, PasswordEncoder passwordEncoder) {
        log.info("Created deny authentication provider");
        return DEFAULT_PROVIDER;
    }

    @Override
    public String getProviderKey() {
        return "deny";
    }

    @Override
    public Set<AuthReaderType> getRequeiredAuthReaderTypes() {
        return Set.of();
    }
}

