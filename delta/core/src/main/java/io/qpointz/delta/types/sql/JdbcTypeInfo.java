package io.qpointz.delta.types.sql;

import java.sql.JDBCType;

public record JdbcTypeInfo(int typeCode, boolean nullable, int prec, int scale) {
    JDBCType getJdbcType() {
        return JDBCType.valueOf(this.typeCode);
    }
}