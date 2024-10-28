package io.qpointz.mill.security.authentication;

import lombok.Getter;

public enum AuthenticationType {

    CUSTOM(0),

    BEARER_TOKEN(100),

    PASSWORD(200);

    @Getter
    private int value;

    AuthenticationType(int value) {
        this.value = value;
    }
}
