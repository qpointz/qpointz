package io.qpointz.delta.types.physical;

import com.google.protobuf.ByteString;
import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.VectorProducerBase;

import java.util.Collection;

public final class BytesPhysical implements PhysicalType<byte[]> {

    public static final BytesPhysical INSTANCE = new BytesPhysical();

    private BytesPhysical() {
    }

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public VectorProducerBase<byte[], Vector.BytesVector.Builder> createVectorProducer() {
        return VectorProducerBase.createProducer(
                Vector.BytesVector::newBuilder,
                new byte[]{},
                (Vector.BytesVector.Builder b, byte[] l) -> b.addValues(ByteString.copyFrom(l)),
                Vector.Builder::setByteVector
        );
    }

}
