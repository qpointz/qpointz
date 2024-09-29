package io.qpointz.mill.sql;

import lombok.val;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public abstract class RecordReaderResultSetBase implements ResultSet {

    private final RecordReader reader;

    public RecordReaderResultSetBase(RecordReader reader) throws SQLException  {
        this.reader = reader;
    }

    private boolean wasNull;

    @Override
    public boolean next() throws SQLException {
        return this.reader.next();
    }

    @Override
    public void close() throws SQLException {
        this.reader.close();
    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.wasNull;
    }

    @Override
    public String getString(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getString(columnIndex);
    }

    @Override
    public boolean getBoolean(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getBoolean(columnIndex);
    }

    @Override
    public byte getByte(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getByte(columnIndex);
    }

    @Override
    public short getShort(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getShort(columnIndex);
    }

    @Override
    public int getInt(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getInt(columnIndex);
    }

    @Override
    public long getLong(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getLong(columnIndex);
    }

    @Override
    public float getFloat(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getFloat(columnIndex);
    }

    @Override
    public double getDouble(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getDouble(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return this.getBigDecimal(columnIndex);
    }

    @Override
    public byte[] getBytes(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getBytes(columnIndex);
    }

    @Override
    public Date getDate(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getDate(columnIndex);
    }

    @Override
    public Time getTime(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getTime(columnIndex);
    }

    @Override
    public Timestamp getTimestamp(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getTimestamp(columnIndex);
    }

    @Override
    public InputStream getAsciiStream(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getAsciiStream(columnIndex);
    }

    @Override
    public InputStream getUnicodeStream(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getUnicodeStream(columnIndex);
    }

    @Override
    public InputStream getBinaryStream(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getBinaryStream(columnIndex);
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return this.reader.getString(this.getColumnIndex(columnLabel));
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return this.reader.getBoolean(this.getColumnIndex(columnLabel));
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return this.reader.getByte(this.getColumnIndex(columnLabel));
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return this.reader.getShort(this.getColumnIndex(columnLabel));
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return this.reader.getInt(this.getColumnIndex(columnLabel));
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return this.reader.getLong(this.getColumnIndex(columnLabel));
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return this.reader.getFloat(this.getColumnIndex(columnLabel));
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return this.reader.getDouble(this.getColumnIndex(columnLabel));
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return this.getBigDecimal(getColumnIndex(columnLabel));
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return this.reader.getBytes(this.getColumnIndex(columnLabel));
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return this.reader.getDate(this.getColumnIndex(columnLabel));
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return this.reader.getTime(this.getColumnIndex(columnLabel));
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return this.reader.getTimestamp(this.getColumnIndex(columnLabel));
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return this.reader.getAsciiStream(this.getColumnIndex(columnLabel));
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return this.reader.getUnicodeStream(this.getColumnIndex(columnLabel));
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return this.reader.getBinaryStream(this.getColumnIndex(columnLabel));
    }

    protected SQLException operationNotSupported() {
        return new SQLFeatureNotSupportedException("Operation not supported");
    }


    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        if (this.reader.hasNext()) {
            return new RecordReaderMetaData(this.reader);
        }
        throw new SQLException("No data available");
    }

    @Override
    public Object getObject(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getObject(columnIndex);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return this.reader.getObject(this.getColumnIndex(columnLabel));
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return this.getColumnIndex(columnLabel)+1;
    }

    public int getColumnIndex(String columnLabel) throws SQLException {
        return this.reader.getColumnIndex(columnLabel);
    }

    @Override
    public Reader getCharacterStream(int columnIdx) throws SQLException {
        return new InputStreamReader(this.getUnicodeStream(columnIdx), StandardCharsets.UTF_8);
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return this.getCharacterStream(this.findColumn(columnLabel));
    }

    @Override
    public BigDecimal getBigDecimal(int columnIdx) throws SQLException {
        val columnIndex = columnIdx - 1;
        this.wasNull = this.reader.isNull(columnIndex);
        return this.reader.getBigDecimal(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return this.reader.getBigDecimal(this.findColumn(columnLabel));
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
           throw operationNotSupported();
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw operationNotSupported();
    }

    /////////////////////////////////////////////////
    ///DEFAULTS ///////////
    ///

    @Override
    public boolean absolute(int row) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public boolean previous() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public int getType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void insertRow() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateRow() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void deleteRow() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void refreshRow() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public int getHoldability() throws SQLException {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw operationNotSupported();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw operationNotSupported();
    }
}
