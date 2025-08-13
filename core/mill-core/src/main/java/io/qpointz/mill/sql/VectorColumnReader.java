package io.qpointz.mill.sql;

import io.qpointz.mill.proto.Vector;

public abstract class VectorColumnReader extends ColumnReader {

    private final Vector vector;

    protected VectorColumnReader(Vector vector) {
        this.vector = vector;
    }

    protected Vector getVector() {
        return this.vector;
    }

    @Override
    public boolean isNull(int rowIdx) {
        return this.vector.getNulls().getNulls(rowIdx);
    }

    protected Long getVectorLong(int rowIdx) {
        return this.getVector().getI64Vector().getValues(rowIdx);
    }

    protected byte[] getVectorBytes(int rowIdx) {
        return this.getVector().getByteVector().getValues(rowIdx).toByteArray();
    }

    protected String getVectorString(int rowIdx) {
        return this.getVector().getStringVector().getValues(rowIdx);
    }

    protected Double getVectorDouble(int rowIdx) {
        return this.getVector().getFp64Vector().getValues(rowIdx);
    }

    protected Float getVectorFloat(int rowIdx) {
        return this.getVector().getFp32Vector().getValues(rowIdx);
    }

    protected Boolean getVectorBoolean(int rowIdx) {
        return this.getVector().getBoolVector().getValues(rowIdx);
    }

    protected Integer getVectorInt(int rowIdx) {
        return this.getVector().getI32Vector().getValues(rowIdx);
    }
}
