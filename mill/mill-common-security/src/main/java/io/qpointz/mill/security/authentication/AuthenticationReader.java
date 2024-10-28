package io.qpointz.mill.security.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.annotation.Nullable;

public interface AuthenticationReader {

    @Nullable
    Authentication readAuthentication(AuthenticationContext context) throws AuthenticationException;

}
