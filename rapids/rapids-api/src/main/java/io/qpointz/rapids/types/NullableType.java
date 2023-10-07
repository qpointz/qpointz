package io.qpointz.rapids.types;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Optional;

@Accessors(fluent=true)
public abstract class NullableType implements RapidsType {

    protected NullableType(Boolean nullable, Optional<Object> defaultValue) {
        this.nullable = nullable;
        this.defaultValue = defaultValue;
    }

    @Getter
    private final Boolean nullable;

    @Getter(AccessLevel.PRIVATE)
    private final Optional<Object> defaultValue;

    Object getDefault() {
        if (defaultValue.isPresent()) {
            return defaultValue.get();
        }
        throw new IllegalStateException("No default value present or type is not nullable");
    }

}
