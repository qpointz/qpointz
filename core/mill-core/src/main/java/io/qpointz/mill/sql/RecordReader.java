package io.qpointz.mill.sql;

import io.qpointz.mill.sql.readers.vector.ColumnMetadata;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public interface RecordReader {
    boolean hasNext();
    boolean next();
    boolean isNull(int columnIndex);
    String getString(int columnIndex);
    Boolean getBoolean(int columnIndex);
    byte getByte(int columnIndex);
    short getShort(int columnIndex);
    int getInt(int columnIndex);
    long getLong(int columnIndex);
    float getFloat(int columnIndex);
    double getDouble(int columnIndex);
    BigDecimal getBigDecimal(int columnIndex);
    byte[] getBytes(int columnIndex);
    Date getDate(int columnIndex);
    Time getTime(int columnIndex);
    Timestamp getTimestamp(int columnIndex);
    InputStream getAsciiStream(int columnIndex);
    InputStream getUnicodeStream(int columnIndex);
    InputStream getBinaryStream(int columnIndex);
    Object getObject(int columnIndex);
    int getColumnIndex(String columnLabel);
    void close();
    int getColumnCount();
    ColumnMetadata getColumnMetadata(int columnIndex);
}
