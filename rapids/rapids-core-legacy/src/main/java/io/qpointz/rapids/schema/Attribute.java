package io.qpointz.rapids.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.calcite.rel.type.RelDataType;

@AllArgsConstructor
@Builder
public class Attribute {

    @Getter
    private final int index;

    @Getter
    private final String name;

    @Getter
    private final RelDataType type;

}
