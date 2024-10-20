package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.ColumnReader;
import io.qpointz.mill.sql.VectorColumnReader;

public class BigIntColumnVectorReader extends VectorColumnReader {

    public BigIntColumnVectorReader(Vector vector) {
        super(vector);
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getVectorLong(rowIdx);
    }

    @Override
    public long getLong(int rowIdx) {
        return this.getVectorLong(rowIdx);
    }

    @Override
    public int getInt(int rowIdx) {
        return this.getVectorLong(rowIdx)
                .intValue();
    }

    @Override
    public short getShort(int rowIdx) {
        return this.getVectorLong(rowIdx)
                .shortValue();
    }

    @Override
    public byte getByte(int rowIdx) {
        return this.getVectorLong(rowIdx)
                .byteValue();
    }

    @Override
    public float getFloat(int rowIdx) {
        return this.getVectorLong(rowIdx)
                .floatValue();
    }

    @Override
    public double getDouble(int rowIdx) {
        return this.getVectorLong(rowIdx)
                .doubleValue();
    }
}
