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
        throw new SQLFeatureNotSupportedException();
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
    public int getResultSetHoldability() {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    private SQLException paramsNotSupportedException() {
        return new SQLException("Parameters not supported");
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void clearParameters() throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void addBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void close() throws SQLException {
        //there is no closable resource to be closed
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void cancel() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
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
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        throw new SQLFeatureNotSupportedException();
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
        throw paramsNotSupportedException();
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        throw paramsNotSupportedException();
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
        throw paramsNotSupportedException();
    }

    @Override
    public boolean getBoolean(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public byte getByte(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public short getShort(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public int getInt(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public long getLong(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public float getFloat(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public double getDouble(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public byte[] getBytes(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Date getDate(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Time getTime(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Object getObject(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Ref getRef(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Blob getBlob(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Clob getClob(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Array getArray(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public URL getURL(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setURL(String parameterName, URL val) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setShort(String parameterName, short x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setInt(String parameterName, int x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setLong(String parameterName, long x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setString(String parameterName, String x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setDate(String parameterName, Date x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setTime(String parameterName, Time x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setObject(String parameterName, Object x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public String getString(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public boolean getBoolean(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public byte getByte(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public short getShort(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public int getInt(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public long getLong(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public float getFloat(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public double getDouble(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public byte[] getBytes(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Date getDate(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Time getTime(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Timestamp getTimestamp(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Object getObject(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Ref getRef(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Blob getBlob(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Clob getClob(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Array getArray(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public URL getURL(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public RowId getRowId(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public RowId getRowId(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setRowId(String parameterName, RowId x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNString(String parameterName, String value) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNClob(String parameterName, NClob value) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public NClob getNClob(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public NClob getNClob(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public SQLXML getSQLXML(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public String getNString(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public String getNString(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Reader getNCharacterStream(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public Reader getCharacterStream(String parameterName) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBlob(String parameterName, Blob x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setClob(String parameterName, Clob x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setClob(String parameterName, Reader reader) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public void setNClob(String parameterName, Reader reader) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        throw paramsNotSupportedException();
    }

    @Override
    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        throw paramsNotSupportedException();
    }
}
