package io.qpointz.mill.sql;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.LogicalDataType;
import io.qpointz.mill.sql.readers.vector.ColumnMetadata;
import lombok.val;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

public class RecordReaderMetaData implements ResultSetMetaData {

    private final RecordReader reader;

    public RecordReaderMetaData(RecordReader reader) {
        this.reader = reader;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return this.reader.getColumnCount();
    }

    private ColumnMetadata getColumnMetaData(int column) {
        return this.reader.getColumnMetadata(column-1);
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return this.getColumnMetaData(column).isAutoIncrement();
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return this.getColumnMetaData(column).isCaseSensitive();
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return this.getColumnMetaData(column).isSearchable();
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return this.getColumnMetaData(column).isCurrency();
    }

    @Override
    public int isNullable(int column) throws SQLException {
        val nullability = this.getColumnMetaData(column).getIsNullable();
        if (nullability == DataType.Nullability.NOT_NULL) {
            return ResultSetMetaData.columnNoNulls;
        } else if (nullability == DataType.Nullability.NULL) {
            return ResultSetMetaData.columnNullable;
        } else {
            return ResultSetMetaData.columnNullableUnknown;
        }
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return this.getColumnMetaData(column).isSigned();
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return this.getColumnMetaData(column).getDisplaySize();
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return this.getColumnMetaData(column).getName();
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return this.getColumnMetaData(column).getName();
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        return "";
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        return this.getColumnMetaData(column).getPrecision();
    }

    @Override
    public int getScale(int column) throws SQLException {
        return this.getColumnMetaData(column).getScale();
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return "";
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        return "";
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        LogicalDataType.LogicalDataTypeId typeId = this.getColumnMetaData(column).getLogicalTypeId();
        return switch (typeId) {
            case NOT_SPECIFIED_TYPE -> Types.OTHER;
            case TINY_INT ->  Types.TINYINT;
            case SMALL_INT -> Types.SMALLINT;
            case INT -> Types.INTEGER;
            case BIG_INT -> Types.BIGINT;
            case BINARY -> Types.BINARY;
            case BOOL -> Types.BLOB;
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

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return this.getColumnMetaData(column).getTypeName();
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return true;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return false;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return this.getColumnMetaData(column).getClassName();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
