package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.sql.VectorColumnReader;

public class DoubleColumnVectorReader extends VectorColumnReader {
    public DoubleColumnVectorReader(Vector vector) {
        super(vector);
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getVectorDouble(rowIdx);
    }

    @Override
    public double getDouble(int rowIdx) {
        return this.getVectorDouble(rowIdx);
    }

    @Override
    public float getFloat(int rowIdx) {
        return this.getVectorDouble(rowIdx)
                .floatValue();
    }

    @Override
    public long getLong(int rowIdx) {
        return this.getVectorDouble(rowIdx)
                .longValue();
    }

    @Override
    public int getInt(int rowIdx) {
        return this.getVectorDouble(rowIdx)
                .intValue();
    }

    @Override
    public short getShort(int rowIdx) {
        return this.getVectorDouble(rowIdx)
                .shortValue();
    }

    @Override
    public byte getByte(int rowIdx) {
        return this.getVectorDouble(rowIdx)
                .byteValue();
    }

}
