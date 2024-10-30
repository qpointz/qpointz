package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Vector;
import io.qpointz.mill.types.logical.UUIDLogical;

import java.util.UUID;

public class UUIDColumnVectorReader extends ConvertingVectorColumnReader<UUID, byte[]> {

    public UUIDColumnVectorReader(Vector vector) {
        super(vector, UUIDLogical.DEFAULT_CONVERTER);
    }

    @Override
    protected byte[] getVectorValue(int rowIdx) {
        return this.getVectorBytes(rowIdx);
    }
}
