package io.qpointz.mill.sql;

import io.qpointz.mill.proto.LogicalDataType;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcUtilsTest {

    @Test
    void shouldMapBoolToJdbcBoolean() {
        int jdbcType = JdbcUtils.logicalTypeIdToJdbcTypeId(LogicalDataType.LogicalDataTypeId.BOOL);
        assertEquals(Types.BOOLEAN, jdbcType);
    }
}
