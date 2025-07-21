package io.qpointz.mill.security.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface AuthenticationMethodDescriptor {

    @JsonProperty("authType")
    AuthenticationType getAuthenticationType();

}
