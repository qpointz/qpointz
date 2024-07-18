package io.qpointz.mill.service.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Set;

public class NoopSecurityProviderFactory implements SecurityProviderFactory<NoopAuthenticationProvider> {

    @Override
    public String getProviderKey() {
        return "noop";
    }

    @Override
    public Set<AuthReaderType> getRequeiredAuthReaderTypes() {
        return Set.of(AuthReaderType.BasicGrpc);
    }

    private static NoopAuthenticationProvider DEFAULT = new NoopAuthenticationProvider();

    @Override
    public NoopAuthenticationProvider createAuthenticationProvider(Map<String, Object> config, PasswordEncoder passwordEncoder) {
        return DEFAULT;
    }
}
