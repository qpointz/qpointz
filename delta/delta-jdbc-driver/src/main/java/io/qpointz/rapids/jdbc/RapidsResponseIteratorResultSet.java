package io.qpointz.rapids.jdbc;

import io.qpointz.rapids.grpc.*;
import io.qpointz.rapids.grpc.Vector;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class RapidsResponseIteratorResultSet implements ResultSet {
    private final Iterator<ExecQueryResponse> iterator;

    private int responseIdx = -1;
    private int recordIdx = -1;
    private ExecQueryResponse currentResponse = null;
    private Schema schema;
    private Field[] fields;
    private Map<String, Integer> labels;
    private boolean wasNull;


    public RapidsResponseIteratorResultSet(Iterator<ExecQueryResponse> responseIterator) {
        this.iterator = responseIterator;
    }

    private void throwSqlException(String message, Object... args) throws SQLException {
        throw new SQLException(String.format(message, args));
    }

    @Override
    public boolean next() throws SQLException {
        if (this.currentResponse == null) {

          if (!this.iterator.hasNext()) {
              return false;
          }

          final var response = this.iterator.next();
          if (response.getStatus().getCode()!= ResponseCode.OK) {
              throw new SQLException(response.getStatus().getMessage());
          }

          if (response.hasSchema()) {
              this.setSchema(response.getSchema());
          }

          if (!response.hasVector()) {
              return this.next();
          }

          this.currentResponse = response;
          this.recordIdx = -1;
          this.responseIdx += 1;
        }

        this.recordIdx +=1;
        if (this.recordIdx>= vectorSize()) {
            this.currentResponse = null;
            this.recordIdx = -1;
            return this.next();
        }

        return true;
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.wasNull;
    }

    private void setSchema(Schema schema) {
        this.schema = schema;
        final var fields = schema.getFieldsList();
        this.fields =  new Field[fields.size()];
        this.labels = new HashMap<>();
        fields.forEach(k-> {
            this.fields[k.getIndex()] = k;
            this.labels.put(k.getName(), k.getIndex());
        });
    }

    private int vectorSize() {
        if (this.currentResponse==null) {
            return -1;
        }

        return this.currentResponse
                .getVector()
                .getVectorSize();
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        final var idx = this.labels.getOrDefault(columnLabel, -1);
        if (idx==-1) {
            throwSqlException("Column '%s' doesn't exists");
        }
        return idx;
    }

    private <T> T get(VectorReader<T> reader, int columnIndex) throws SQLException {
        try {
            final var mayBeVal = reader.read(this.currentResponse, columnIndex, recordIdx);
            this.wasNull = mayBeVal.isEmpty();
            return reader.valueOrNull(mayBeVal);
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    private <T> T get(VectorReader<T> reader, String columnLabel) throws SQLException {
        return this.get(reader, findColumn(columnLabel));
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return this.get(RapidsTypes.STRING.VECTOR_READER, columnLabel);
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return this.get(RapidsTypes.STRING.VECTOR_READER, columnIndex);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return this.get(RapidsTypes.INT32.VECTOR_READER, columnLabel);
    }
    @Override
    public int getInt(int columnIndex) throws SQLException {
        return this.get(RapidsTypes.INT32.VECTOR_READER, columnIndex);
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return this.get(RapidsTypes.INT64.VECTOR_READER, columnIndex);
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return this.get(RapidsTypes.INT64.VECTOR_READER, columnLabel);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return this.get(RapidsTypes.BOOLEAN.VECTOR_READER, columnIndex);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return this.get(RapidsTypes.BOOLEAN.VECTOR_READER, columnLabel);
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return this.get(RapidsTypes.FLOAT.VECTOR_READER, columnIndex);
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return this.get(RapidsTypes.FLOAT.VECTOR_READER, columnLabel);
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return this.get(RapidsTypes.DOUBLE.VECTOR_READER, columnIndex);
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return this.get(RapidsTypes.DOUBLE.VECTOR_READER, columnLabel);
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return this.get(RapidsTypes.BYTES.VECTOR_READER, columnIndex).toByteArray();
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return this.get(RapidsTypes.BYTES.VECTOR_READER, columnLabel).toByteArray();
    }
    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return Date.valueOf(this.get(RapidsTypes.DATE.VECTOR_READER, columnIndex));
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return Date.valueOf(this.get(RapidsTypes.DATE.VECTOR_READER, columnLabel));
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throwSqlException("Calendar operation not supported");
        return null;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        throwSqlException("Calendar operation not supported");
        return null;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return Time.valueOf(this.get(RapidsTypes.TIME.VECTOR_READER, columnIndex));
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return Time.valueOf(this.get(RapidsTypes.TIME.VECTOR_READER, columnLabel));
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return Timestamp.valueOf(this.get(RapidsTypes.DATETIME.VECTOR_READER, columnIndex));
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return Timestamp.valueOf(this.get(RapidsTypes.DATETIME.VECTOR_READER, columnLabel));
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return (byte)this.getInt(columnIndex);
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return (byte)this.getInt(columnLabel);
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return (short)this.getInt(columnIndex);
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return (short)this.getInt(columnLabel);
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return this.responseIdx<=0 && this.recordIdx==-1;
    }

    @Override
    public boolean isFirst() throws SQLException {
        return this.responseIdx==0 && this.recordIdx==0;
    }

    @Override
    public boolean isLast() throws SQLException {
        return !this.iterator.hasNext() && (this.vectorSize()-this.recordIdx)<=1;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return !this.iterator.hasNext() && this.recordIdx==-1;
    }

    /* update methods */

    private void updateColumnException(String columnLabel) throws SQLException {
        throwSqlException("Update column '%s' operation not supported", columnLabel);
    }

    private void updateColumnException(int columnIndex) throws SQLException {
        throwSqlException("Update column [%d] operation not supported", columnIndex);
    }


    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateRow() throws SQLException {
        throwSqlException("Row update operation not supported");
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throwSqlException("Cancel row update operation not supported");
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        updateColumnException(columnLabel);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        updateColumnException(columnIndex);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        updateColumnException(columnLabel);
    }

    /*==========================================================*/

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public String getCursorName() throws SQLException {
        return null;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void beforeFirst() throws SQLException {

    }

    @Override
    public void afterLast() throws SQLException {

    }

    @Override
    public boolean first() throws SQLException {
        return false;
    }

    @Override
    public boolean last() throws SQLException {
        return false;
    }

    @Override
    public int getRow() throws SQLException {
        return 0;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return false;
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return false;
    }

    @Override
    public boolean previous() throws SQLException {
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

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getType() throws SQLException {
        return 0;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    @Override
    public void insertRow() throws SQLException {

    }

    @Override
    public void deleteRow() throws SQLException {

    }

    @Override
    public void refreshRow() throws SQLException {

    }

    @Override
    public void moveToInsertRow() throws SQLException {

    }

    @Override
    public void moveToCurrentRow() throws SQLException {

    }

    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    /*==========================================================*/

}
