package io.qpointz.mill.service.descriptors;

import lombok.Getter;

public enum AuthMethod {

    CUSTOM(0),

    OAUTH2(100),

    BASIC(300);

    @Getter
    private int value;

    AuthMethod(int value) {
        this.value = value;
    }

    public static AuthMethod valueOf(Integer value) {
        return switch (value) {
            case  0 -> CUSTOM;
            case 100 -> OAUTH2;
            case 300 -> BASIC;
            default -> throw new IllegalArgumentException("No auth method for value of:"+value.toString());
        };
    }

}
