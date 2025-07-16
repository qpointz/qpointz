package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.VectorColumnReader;

public class SmallIntColumnVectorReader extends VectorColumnReader {

    public SmallIntColumnVectorReader(Vector vector) {
        super(vector);
    }

    private short getVectorValue(int rowIdx) {
        return this.getVectorInt(rowIdx).shortValue();
    }

    @Override
    public int getInt(int rowIdx) {
        return this.getVectorInt(rowIdx);
    }

    @Override
    public short getShort(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }

    @Override
    public byte getByte(int rowIdx) {
        return (byte) this.getShort(rowIdx);
    }

    @Override
    public float getFloat(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }

    @Override
    public double getDouble(int rowIdx) {
        return this.getVectorValue(rowIdx);
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getShort(rowIdx);
    }
}
