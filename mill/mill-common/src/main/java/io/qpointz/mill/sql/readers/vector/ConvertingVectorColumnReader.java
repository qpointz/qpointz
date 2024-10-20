package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.sql.VectorColumnReader;
import io.qpointz.mill.types.conversion.ValueConverter;

public abstract class ConvertingVectorColumnReader<L,P> extends VectorColumnReader {

    private final ValueConverter<L,P> converter;

    public ConvertingVectorColumnReader(Vector vector, ValueConverter<L,P> converter) {
        super(vector);
        this.converter = converter;
    }

    protected ValueConverter<L,P> getConverter() {
        return this.converter;
    }

    protected abstract P getVectorValue(int rowIdx);

    protected L getValue(int rowIdx) {
        return this.getConverter().from(getVectorValue(rowIdx));
    }

    @Override
    public Object getObject(int rowIdx) {
        return this.getValue(rowIdx);
    }
}
