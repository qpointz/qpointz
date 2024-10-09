package io.qpointz.mill;

import io.qpointz.mill.client.MillSqlQuery;
import lombok.extern.java.Log;
import lombok.val;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

@Log
public class MillCallableStatement implements CallableStatement {

    private MillSqlQuery.MillSqlQueryBuilder queryBuilder = null;

    private ResultSet resultSet = null;

    public MillCallableStatement(MillConnection millConnection) {
        this(MillSqlQuery.builder()
                .connection(millConnection));
    }

    public MillCallableStatement(MillConnection millConnection, String sql) {
        this(millConnection);
        this.queryBuilder.sql(sql);
    }

    MillCallableStatement(MillSqlQuery.MillSqlQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.queryBuilder.build().getConnection();
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        val millQuery = this.queryBuilder
                .build();
        try {
            this.resultSet = new MillRecordReaderResultSet(millQuery.executeRecordIterator());
        } catch (MillCodeException e) {
            throw e.asSqlException();
        }
        return this.resultSet;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        log.info(String.format("Executing %s", sql));
        this.queryBuilder.sql(sql);
        return this.executeQuery();
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        this.queryBuilder
                .sql(sql);
        this.executeQuery();
        return true;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        this.queryBuilder
                .sql(sql)
                .selectedIndexed(columnIndexes);
        this.executeQuery();
        return true;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        this.queryBuilder
                .sql(sql)
                .selectedNamesArray(columnNames);
        this.executeQuery();
        return true;
    }

    @Override
    public boolean execute() throws SQLException {
        this.executeQuery();
        return true;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return this.getResultSet().getMetaData();
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return 0;
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        this.queryBuilder.sql(sql);
        this.executeQuery();
        return true;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return this.resultSet == null
                ? this.executeQuery()
                : this.resultSet;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return -1;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        this.queryBuilder.fetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return this.queryBuilder.build().getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return new int[0];
    }


    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public int executeUpdate() throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    private void throwParamsNotSupported() throws SQLException {
        throw new SQLException("Parameters not supported");
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void clearParameters() throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void addBatch() throws SQLException {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {

    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {

    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {

    }

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {

    }

    @Override
    public void addBatch(String sql) throws SQLException {

    }

    @Override
    public void clearBatch() throws SQLException {

    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public boolean wasNull() throws SQLException {
        if (this.resultSet !=null) {
            return this.resultSet.wasNull();
        }
        throw new SQLException("No result set");
    }

    @Override
    public String getString(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public boolean getBoolean(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return false;
    }

    @Override
    public byte getByte(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public short getShort(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public int getInt(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public long getLong(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public float getFloat(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public double getDouble(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public byte[] getBytes(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return new byte[0];
    }

    @Override
    public Date getDate(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Time getTime(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Object getObject(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Ref getRef(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Blob getBlob(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Clob getClob(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Array getArray(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public URL getURL(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public void setURL(String parameterName, URL val) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setShort(String parameterName, short x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setInt(String parameterName, int x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setLong(String parameterName, long x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setString(String parameterName, String x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setDate(String parameterName, Date x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setTime(String parameterName, Time x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setObject(String parameterName, Object x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public String getString(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return "";
    }

    @Override
    public boolean getBoolean(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return false;
    }

    @Override
    public byte getByte(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public short getShort(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public int getInt(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public long getLong(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public float getFloat(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public double getDouble(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return 0;
    }

    @Override
    public byte[] getBytes(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return new byte[0];
    }

    @Override
    public Date getDate(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Time getTime(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Timestamp getTimestamp(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Object getObject(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Ref getRef(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Blob getBlob(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Clob getClob(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Array getArray(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public URL getURL(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public RowId getRowId(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public RowId getRowId(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public void setRowId(String parameterName, RowId x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNString(String parameterName, String value) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNClob(String parameterName, NClob value) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public NClob getNClob(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public NClob getNClob(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public SQLXML getSQLXML(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public String getNString(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return "";
    }

    @Override
    public String getNString(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return "";
    }

    @Override
    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Reader getNCharacterStream(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public Reader getCharacterStream(String parameterName) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public void setBlob(String parameterName, Blob x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setClob(String parameterName, Clob x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setClob(String parameterName, Reader reader) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public void setNClob(String parameterName, Reader reader) throws SQLException {
        throwParamsNotSupported();
    }

    @Override
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        throwParamsNotSupported();
        return null;
    }

    @Override
    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        throwParamsNotSupported();
        return null;
    }
}
