package io.qpointz.rapids.types;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;

public interface RapidsType {

    RelDataType asRelDataType(RelDataTypeFactory typeFactory);

    Boolean nullable();

}
