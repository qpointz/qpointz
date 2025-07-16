package io.qpointz.mill.security.authentication.token;

import io.qpointz.mill.security.authentication.AuthenticationReader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

import java.util.function.Supplier;

@AllArgsConstructor
@Builder
@Slf4j
public class BearerTokenAuthenticationReader implements AuthenticationReader {

    @Builder.Default
    private String prefix = "Bearer";

    @Getter
    private Supplier<String> tokenSupplier;

    @Override
    public Authentication readAuthentication() throws AuthenticationException {
        val header = tokenSupplier.get();

        if (header == null || header.isEmpty() || !header.toLowerCase().startsWith(prefix.toLowerCase())) {
            log.debug("No value for 'Bearer' authentication provided");
            return null;
        }

        val token = header.substring(prefix.length()+1);

        return new BearerTokenAuthenticationToken(token);
    }
}
