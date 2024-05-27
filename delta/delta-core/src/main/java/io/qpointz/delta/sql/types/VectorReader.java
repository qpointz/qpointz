package io.qpointz.delta.sql.types;

import io.qpointz.delta.proto.ExecQueryResponse;
import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.proto.VectorBlock;

public interface VectorReader<T> {

    T read(Vector vector, int rowIdx);

    default T read( VectorBlock vectorBlock, int columnIdx, int rowIdx) {
        return read(vectorBlock.getVectors(columnIdx), rowIdx);
    }

    default T read(ExecQueryResponse response, int columnIdx, int rowIdx) {
        return read(response.getVector(), columnIdx, rowIdx);
    }

    boolean isNull(Vector vector, int rowIdx);

    default boolean isNull( VectorBlock vectorBlock, int columnIdx, int rowIdx) {
        return isNull(vectorBlock.getVectors(columnIdx), rowIdx);
    }

    default boolean isNull(ExecQueryResponse response, int columnIdx, int rowIdx) {
        return isNull(response.getVector(), columnIdx, rowIdx);
    }

}
