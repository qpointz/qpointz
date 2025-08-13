package io.qpointz.mill.security.authentication;

import lombok.Getter;

public enum AuthenticationType {

    CUSTOM(0),

    OAUTH2(100),

    BASIC(300);

    @Getter
    private int value;

    AuthenticationType(int value) {
        this.value = value;
    }
}
