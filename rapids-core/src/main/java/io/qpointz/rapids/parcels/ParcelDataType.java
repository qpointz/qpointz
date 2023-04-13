package io.qpointz.rapids.parcels;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;

public abstract class ParcelDataType {

    abstract public RelDataType asRelDataType(RelDataTypeFactory typeFactory);


}
