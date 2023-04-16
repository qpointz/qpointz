package io.qpointz.rapids.types;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Accessors(fluent=true)
public class IntType extends NullableType {

    public IntType(Boolean nullable, Optional<Object> defaultValue) {
        super(nullable, defaultValue);
    }

    @Override
    public RelDataType asRelDataType(RelDataTypeFactory typeFactory) {
        return typeFactory.createJavaType(Integer.class);
    }
}
