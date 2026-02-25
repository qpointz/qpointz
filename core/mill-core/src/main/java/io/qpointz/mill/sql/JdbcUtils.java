package io.qpointz.mill.sql;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.LogicalDataType;

import java.sql.ResultSetMetaData;
import java.sql.Types;

public class JdbcUtils {

    private JdbcUtils() {
        //hiding constructor
    }

    private static final String NO_LABEL = "NO";

    private static final String YES_LABEL = "YES";

    private static final String NULL_LABEL = "";

    public static String logicalTypeIdToJdbcTypeName(LogicalDataType.LogicalDataTypeId typeId) {
        return typeId.name();
    }

    public static int logicalTypeIdToJdbcTypeId(LogicalDataType.LogicalDataTypeId typeId) {
        return switch (typeId) {
            case NOT_SPECIFIED_TYPE -> Types.OTHER;
            case TINY_INT ->  Types.TINYINT;
            case SMALL_INT -> Types.SMALLINT;
            case INT -> Types.INTEGER;
            case BIG_INT -> Types.BIGINT;
            case BINARY -> Types.BINARY;
            case BOOL -> Types.BOOLEAN;
            case DATE -> Types.DATE;
            case FLOAT -> Types.FLOAT;
            case DOUBLE -> Types.DOUBLE;
            case INTERVAL_DAY -> Types.BIGINT;
            case INTERVAL_YEAR -> Types.BIGINT;
            case STRING -> Types.NVARCHAR;
            case TIMESTAMP -> Types.TIMESTAMP;
            case TIMESTAMP_TZ -> Types.TIMESTAMP_WITH_TIMEZONE;
            case TIME -> Types.TIME;
            case UUID -> Types.BINARY;
            case UNRECOGNIZED -> Types.OTHER;
        };
    }

    public static int jdbcNullability(DataType.Nullability nullability) {
        if (nullability == DataType.Nullability.NOT_NULL) {
            return ResultSetMetaData.columnNoNulls;
        } else if (nullability == DataType.Nullability.NULL) {
            return ResultSetMetaData.columnNullable;
        } else {
            return ResultSetMetaData.columnNullableUnknown;
        }
    }

    public static String jdbcNullabilityLabel(DataType.Nullability nullability) {
        if (nullability == DataType.Nullability.NOT_NULL) {
            return NO_LABEL;
        } else if (nullability == DataType.Nullability.NULL) {
            return YES_LABEL;
        } else {
            return NULL_LABEL;
        }
    }
}
