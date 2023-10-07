package io.qpointz.rapids.parcels;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;

public interface ParcelDataType {

    RelDataType asRelDataType(RelDataTypeFactory typeFactory);


}
