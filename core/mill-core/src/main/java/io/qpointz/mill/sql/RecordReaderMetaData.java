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
        return JdbcUtils.jdbcNullability(nullability);
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
        return JdbcUtils.logicalTypeIdToJdbcTypeId(typeId);
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
