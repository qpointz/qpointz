package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

abstract class VectorConsumer<TValue> {
    private int bufferSize;
    private List<TValue> values;
    private List<Boolean> nulls;

    public void init(int bufferSize) {
        this.bufferSize = bufferSize;
        this.reset();
    }

    public void reset() {
        this.values = new ArrayList<>(this.bufferSize);
        this.nulls = new ArrayList<>(this.bufferSize);
    }

    public void vector(Vector.Builder vectorBuilder) {
        this.vector(vectorBuilder, this.values, this.nulls);
    }

    protected abstract void vector(Vector.Builder vectorBuilder, Iterable<TValue> values, Iterable<Boolean> nulls);

    public void read(ResultSet resultSet, int columnIndex, int rowIndex) throws SQLException {
        final var value = this.getValue(resultSet, columnIndex);
        final var isnull = resultSet.wasNull();
        this.nulls.add(isnull);
        this.values.add(isnull ? this.nullValue() : value);
    }

    protected abstract TValue getValue(ResultSet resultSet, int columnIndex) throws SQLException;

    protected abstract TValue nullValue();
}
