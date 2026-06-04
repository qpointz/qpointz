package io.qpointz.mill.security.authentication.basic;

import io.qpointz.mill.security.authentication.AuthenticationReader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Base64;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

@AllArgsConstructor
@Builder
@Slf4j
public class BasicAuthenticationReader implements AuthenticationReader {

    @Builder.Default
    private String prefix = "Basic";

    @Getter
    private Supplier<String> tokenSupplier;

    @Override
    public Authentication readAuthentication() throws AuthenticationException {
        val header = tokenSupplier.get();
        if (header == null || header.isEmpty() || !header.toLowerCase().startsWith(prefix.toLowerCase())) {
            log.debug("No Basic Authorization header present");
            return null;
        }

        val token = header.substring(prefix.length()+1).getBytes(UTF_8);
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(token);
        } catch (final IllegalArgumentException e) {
            log.warn("Basic Authorization header is not valid Base64");
            throw new BadCredentialsException("Invalid 'Basic' token. Decode failure.");
        }

        final String decodedToken = new String(decoded, UTF_8);
        val indexOf = decodedToken.indexOf(':');
        if (indexOf == -1) {
            log.warn("Basic Authorization header is missing username/password separator");
            throw new BadCredentialsException("Invalid 'Basic' token. Malformed token.");
        }

        val username = decodedToken.substring(0, indexOf);
        log.debug("Basic Authorization header parsed for user '{}'", username);
        return new UsernamePasswordAuthenticationToken(
                username,
                decodedToken.substring(indexOf + 1));

    }

}
