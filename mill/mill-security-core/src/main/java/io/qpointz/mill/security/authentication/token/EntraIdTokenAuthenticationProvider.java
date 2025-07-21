package io.qpointz.mill.security.authentication.token;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;

@Slf4j
public class EntraIdTokenAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        val bearerAuthToken = (BearerTokenAuthenticationToken)authentication;
        val profileLoader = new EntraIdProfileLoader(bearerAuthToken.getToken());
        val profile = profileLoader.getProfile();
        val token = new EntraIdAuthenticationToken(profile, bearerAuthToken.getToken());
        log.debug("token profile {}", profile);
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public class EntraIdAuthenticationToken implements Authentication {

        private final EntraIdProfileLoader.EntraIdProfile profile;
        private final String token;

        public EntraIdAuthenticationToken(EntraIdProfileLoader.EntraIdProfile profile, String token) {
            this.profile = profile;
            this.token = token;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return this.profile.grantedAuthorities();
        }

        @Override
        public Object getCredentials() {
            return this.token;
        }

        @Override
        public Object getDetails() {
            return this.profile;
        }

        @Override
        public Object getPrincipal() {
            return this.getName();
        }

        @Override
        public boolean isAuthenticated() {
            return this.profile!=null;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            throw  new IllegalArgumentException("Operation not supported.");
        }

        @Override
        public String getName() {
            log.info("Current profile:{}", this.profile);
            return this.profile==null ? null : this.profile.principalName();
        }
    }

}
