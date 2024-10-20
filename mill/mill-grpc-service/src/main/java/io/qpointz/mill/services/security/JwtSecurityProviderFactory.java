package io.qpointz.mill.services.security;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

import java.util.Map;
import java.util.Set;

@Slf4j
public class JwtSecurityProviderFactory implements SecurityProviderFactory<JwtAuthenticationProvider> {

    @Override
    public String getProviderKey() {
        return "jwt";
    }

    @Override
    public Set<AuthReaderType> getRequeiredAuthReaderTypes() {
        return Set.of(AuthReaderType.Bearer);
    }

    @Override
    public JwtAuthenticationProvider createAuthenticationProvider(Map<String, Object> config, PasswordEncoder passwordEncoder) {
        val issuerUri = config.get("issuer-uri");
        log.info("JWT Issuer URI grp: {}", issuerUri);
        if (issuerUri == null) {
            throw new IllegalArgumentException("'jwt' provider requires 'issuer-uri' configuration to be provided");
        }
        val decoder = new MillJwtDecoder(issuerUri.toString());
        return new JwtAuthenticationProvider(decoder);

    }

    @Slf4j
    public static class MillJwtDecoder implements JwtDecoder {

        private final NimbusJwtDecoder jwtDecoder;

        public MillJwtDecoder(String jwkSetUri) {
            this.jwtDecoder = NimbusJwtDecoder
                    .withJwkSetUri(jwkSetUri).build();
        }

        @Override
        public Jwt decode(String token) throws JwtException {
            try {
                log.debug("token: " + token);
                Jwt jwt = jwtDecoder.decode(token);
                log.debug("jwt: " + jwt);
                return jwt;
            } catch (JwtException e) {
                log.error("Failed to extract token", e);
                return null;
            }
        }

    }
}

