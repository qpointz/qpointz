package io.qpointz.rapids.types;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Accessors(fluent=true)
public class LongType extends NullableType {


    public LongType(Boolean nullable, Optional<Object> defaultValue) {
        super(nullable, defaultValue);
    }

    @Override
    public RelDataType asRelDataType(RelDataTypeFactory typeFactory) {
        return typeFactory.createJavaType(Long.class);
    }

}
