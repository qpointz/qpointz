package io.qpointz.mill.services.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Set;

public interface SecurityProviderFactory<T extends AuthenticationProvider> {

    public String getProviderKey();

    public T createAuthenticationProvider(Map<String,Object> config, PasswordEncoder passwordEncoder);

    public Set<AuthReaderType> getRequeiredAuthReaderTypes();
}