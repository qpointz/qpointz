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
    private String headerName = "Authorization";

    @Builder.Default
    private String prefix = "Basic";

    @Getter
    private Supplier<String> headerValueSupplier;

    @Override
    public Authentication readAuthentication() throws AuthenticationException {
        val header = headerValueSupplier.get();
        if (header == null || header.isEmpty() || !header.toLowerCase().startsWith(prefix.toLowerCase())) {
            log.debug("Not 'Basic' authorization header");
            return null;
        }

        val token = header.substring(prefix.length()+1).getBytes(UTF_8);
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(token);
        } catch (final IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid 'Basic' token.Decode failure.");
        }

        final String decodedToken = new String(decoded, UTF_8);
        val indexOf = decodedToken.indexOf(':');
        if (indexOf == -1) {
            throw new BadCredentialsException("Invalid 'Basic' token.Malformed token.");
        }

        return new UsernamePasswordAuthenticationToken(
                decodedToken.substring(0, indexOf),
                decodedToken.substring(indexOf + 1));

    }

}
