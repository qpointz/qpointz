package io.qpointz.mill.security.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public interface AuthenticationReader {

    Authentication readAuthentication() throws AuthenticationException;

}
