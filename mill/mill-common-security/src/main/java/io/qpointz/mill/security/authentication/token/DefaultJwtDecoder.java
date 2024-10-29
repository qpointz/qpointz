package io.qpointz.mill.security.authentication.token;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Slf4j
public class DefaultJwtDecoder implements JwtDecoder {

    private final NimbusJwtDecoder jwtDecoder;

    public DefaultJwtDecoder(String jwkSetUri) {
        this.jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri).build();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            log.debug("token: " + token);
            val jwt = jwtDecoder.decode(token);
            log.debug("jwt: " + jwt);
            return jwt;
        } catch (JwtException e) {
            log.error("Failed to extract token", e);
            return null;
        }
    }

}
