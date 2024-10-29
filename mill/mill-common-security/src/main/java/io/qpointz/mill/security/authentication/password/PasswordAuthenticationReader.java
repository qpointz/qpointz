package io.qpointz.mill.security.authentication.password;

import io.qpointz.mill.security.authentication.AuthenticationContext;
import io.qpointz.mill.security.authentication.AuthenticationReader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@AllArgsConstructor
@Builder
public class PasswordAuthenticationReader implements AuthenticationReader {

    @Builder.Default
    private String headerName = "Authorization";

    @Builder.Default
    private String prefix = "Basic";

    @Override
    public Authentication readAuthentication(AuthenticationContext context) throws AuthenticationException {
        //UsernamePasswordAuthenticationToken
        return null;
    }
}
