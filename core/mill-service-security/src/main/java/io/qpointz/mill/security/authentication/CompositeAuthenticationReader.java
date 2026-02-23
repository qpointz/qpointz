package io.qpointz.mill.security.authentication;

import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;

public class CompositeAuthenticationReader implements AuthenticationReader {

    private final List<AuthenticationReader> readers;

    public CompositeAuthenticationReader(List<AuthenticationReader> readers) {
        this.readers = readers;
    }

    @Override
    public Authentication readAuthentication() throws AuthenticationException {
        AuthenticationException thrown = null;
        for (AuthenticationReader reader : readers) {
            try {
                thrown = null;
                val authentication = reader.readAuthentication();
                if (authentication != null) {
                    return authentication;
                }
            } catch (AuthenticationException e) {
                thrown = e;
            }
        }
        if (thrown == null) {
            return null;
        } else {
            throw thrown;
        }
    }
}
