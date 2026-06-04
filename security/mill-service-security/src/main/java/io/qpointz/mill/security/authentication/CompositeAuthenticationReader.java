package io.qpointz.mill.security.authentication;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;

@Slf4j
public class CompositeAuthenticationReader implements AuthenticationReader {

    private final List<AuthenticationReader> readers;

    public CompositeAuthenticationReader(List<AuthenticationReader> readers) {
        this.readers = readers;
        log.info("Composite authentication reader initialized with {} delegate(s)", readers.size());
    }

    @Override
    public Authentication readAuthentication() throws AuthenticationException {
        AuthenticationException thrown = null;
        for (AuthenticationReader reader : readers) {
            try {
                thrown = null;
                val authentication = reader.readAuthentication();
                if (authentication != null) {
                    log.debug(
                            "Authentication extracted by {}",
                            reader.getClass().getSimpleName()
                    );
                    return authentication;
                }
            } catch (AuthenticationException e) {
                log.warn(
                        "Authentication reader {} rejected credentials: {}",
                        reader.getClass().getSimpleName(),
                        e.getMessage()
                );
                thrown = e;
            }
        }
        if (thrown == null) {
            log.debug("No authentication extracted from {} reader(s)", readers.size());
            return null;
        } else {
            throw thrown;
        }
    }
}
