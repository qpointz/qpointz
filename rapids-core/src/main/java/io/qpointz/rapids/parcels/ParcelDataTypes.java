package io.qpointz.rapids.parcels;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;

public class ParcelDataTypes {

    public static class ParcelStingDataType extends ParcelDataType {
        @Override
        public RelDataType asRelDataType(RelDataTypeFactory typeFactory) {
            return typeFactory.createSqlType(SqlTypeName.VARCHAR);
        }
    }

    public static class ParcelIntDataType extends ParcelDataType {
        @Override
        public RelDataType asRelDataType(RelDataTypeFactory typeFactory) {
            return typeFactory.createSqlType(SqlTypeName.INTEGER);
        }
    }

    public static class ParcelBooleanDataType extends ParcelDataType {
        @Override
        public RelDataType asRelDataType(RelDataTypeFactory typeFactory) {
            return typeFactory.createSqlType(SqlTypeName.BOOLEAN);
        }
    }

    public static ParcelDataType stringType() {
        return new ParcelStingDataType();
    }

    public static ParcelDataType integerType() {
        return new ParcelIntDataType();
    }

    public static ParcelDataType booleanType() {
        return new ParcelBooleanDataType();
    }

}
