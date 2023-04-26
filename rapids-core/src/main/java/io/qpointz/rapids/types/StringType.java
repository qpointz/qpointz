package io.qpointz.rapids.types;

import lombok.*;
import lombok.experimental.Accessors;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Accessors(fluent=true)
public final class StringType extends NullableType {


    @Getter
    private final Optional<Integer> length;

    public StringType(Boolean nullable, Optional<Object> defaultValue) {
        this(nullable, defaultValue, Optional.empty());
    }

    public StringType(Boolean nullable, Optional<Object> defaultValue, Optional<Integer> length) {
        super(nullable, defaultValue);
        this.length = length;
    }

    @Override
    public RelDataType asRelDataType(RelDataTypeFactory typeFactory) {
        return typeFactory.createJavaType(String.class);
    }



}
