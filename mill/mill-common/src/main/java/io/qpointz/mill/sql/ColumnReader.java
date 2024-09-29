package io.qpointz.mill.sql;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public abstract class ColumnReader {
    public boolean isNull(int rowIdx) {
        throw notSupported();
    }

    private RuntimeException notSupported() {
        return new RuntimeException("Operation not supported");
    }

    public String getString(int rowIdx) {
        throw notSupported();
    }

    public Boolean getBoolean(int rowIdx) {
        throw notSupported();
    }

    public byte getByte(int rowIdx) {
        throw notSupported();
    }

    public short getShort(int rowIdx) {
        throw notSupported();
    }

    public int getInt(int rowIdx) {
        throw notSupported();
    }

    public long getLong(int rowIdx) {
        throw notSupported();
    }

    public float getFloat(int rowIdx) {
        throw notSupported();
    }

    public double getDouble(int rowIdx) {
        throw notSupported();
    }

    public BigDecimal getBigDecimal(int rowIdx) {
        throw notSupported();
    }

    public byte[] getBytes(int rowIdx) {
        throw notSupported();
    }

    public Date getDate(int rowIdx) {
        throw notSupported();
    }

    public Time getTime(int rowIdx) {
        throw notSupported();
    }

    public Timestamp getTimestamp(int rowIdx) {
        throw notSupported();
    }

    public InputStream getAsciiString(int rowIdx) {
        throw notSupported();
    }

    public InputStream getUnicodeString(int rowIdx) {
        throw notSupported();
    }

    public InputStream getBinaryStream(int rowIdx) {
        throw notSupported();
    }

    public Object getObject(int rowIdx) {
        throw notSupported();
    }

}
