package io.qpointz.rapids.parcels;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.Optional;

public class ParcelDataTypes {

    @EqualsAndHashCode
    public static class ParcelStingDataType implements ParcelDataType {

        @Getter
        private final Optional<Integer> mayBeLength;

        public ParcelStingDataType(Optional<Integer> length) {
            this.mayBeLength = length;
        }

        public Boolean hasLength() {
            return this.getMayBeLength().isPresent();
        }

        public int getLength() {
            return this.mayBeLength.get();
        }

        @Override
        public RelDataType asRelDataType(RelDataTypeFactory typeFactory) {
            return typeFactory.createSqlType(SqlTypeName.VARCHAR);
        }
    }

    public static class ParcelIntDataType implements ParcelDataType {
        @Override
        public RelDataType asRelDataType(RelDataTypeFactory typeFactory) {
            return typeFactory.createSqlType(SqlTypeName.INTEGER);
        }
    }

    public static class ParcelBooleanDataType implements ParcelDataType {
        @Override
        public RelDataType asRelDataType(RelDataTypeFactory typeFactory) {
            return typeFactory.createSqlType(SqlTypeName.BOOLEAN);
        }
    }

    public static ParcelDataType stringType() {
        return new ParcelStingDataType(Optional.empty());
    }

    public static ParcelDataType stringType(int length) {
        return new ParcelStingDataType(Optional.of(length));
    }

    public static ParcelDataType integerType() {
        return new ParcelIntDataType();
    }

    public static ParcelDataType booleanType() {
        return new ParcelBooleanDataType();
    }

}
