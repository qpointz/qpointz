package io.qpointz.rapids.grpc;

import java.util.Optional;

public abstract class VectorReader<TValueType> {
    public Optional<TValueType> read(ExecQueryResponse response, int columnIndex, int recordIdx) {
        return this.read(response.getVector(), columnIndex, recordIdx);
    }

    public Optional<TValueType> read(VectorBlock vectorBlock, int columnIndex, int recordIdx) {
        return this.read(vectorBlock.getVectors(columnIndex), recordIdx);
    }
    public abstract Optional<TValueType> read(Vector vector, int recordIdx);

    public abstract TValueType valueOrNull(Optional<TValueType> mayBeValue);
}
