package io.qpointz.rapids.parcels;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.FilterableTable;
import org.apache.calcite.schema.impl.AbstractTable;

public abstract class ParcelTable extends AbstractTable implements FilterableTable {

    public abstract RelDataType getRowType(RelDataTypeFactory typeFactory);

}