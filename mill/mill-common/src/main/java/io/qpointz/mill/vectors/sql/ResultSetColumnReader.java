package io.qpointz.mill.vectors.sql;

import java.sql.*;

public class ResultSetColumnReader {

    private final int columnIndex;

    private final ResultSet resultSet;

    public ResultSetColumnReader(ResultSet resultSet, int columnIndex) {
        this.resultSet = resultSet;
        this.columnIndex = columnIndex;
    }

    public Boolean isNull() {
        try {
            return resultSet.wasNull();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getInt() {
        try {
            return resultSet.getInt(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Short getShort() {
        try {
            return resultSet.getShort(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getLong() {
        try {
            return resultSet.getLong(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean getBoolean() {
        try {
            return resultSet.getBoolean(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getString() {
        try {
            return resultSet.getString(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getBytes() {
        try {
            return resultSet.getBytes(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Date getDate() {
        try {
            return resultSet.getDate(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Float getFloat() {
        try {
            return resultSet.getFloat(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Double getDouble() {
        try {
            return resultSet.getDouble(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Time getTime() {
        try {
            return resultSet.getTime(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Timestamp getTimestamp() {
        try {
            return resultSet.getTimestamp(this.columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
