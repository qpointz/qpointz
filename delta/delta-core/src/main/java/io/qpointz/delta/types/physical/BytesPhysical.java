package io.qpointz.delta.types.physical;

import com.google.protobuf.ByteString;
import io.qpointz.delta.proto.Vector;
import io.qpointz.delta.vectors.PhysicalTypeVectorProducer;

import java.util.Collection;
import java.util.Collections;

public final class BytesPhysical implements PhysicalType<byte[]> {

    public static final BytesPhysical INSTANCE = new BytesPhysical();

    private BytesPhysical() {
    }

    @Override
    public <T> T accept(PhysicalTypeShuttle<T> shuttle) {
        return shuttle.visit(this);
    }

    public PhysicalTypeVectorProducer<byte[], Vector.BytesVector.Builder> createVectorProducer() {
        return PhysicalTypeVectorProducer.createProducer(
                Vector.BytesVector::newBuilder,
                new byte[]{},
                (Vector.BytesVector.Builder b, byte[] l) -> b.addValues(ByteString.copyFrom(l)),
                (Vector.BytesVector.Builder b, Collection<byte[]> l) -> b.addAllValues(l.stream().map(ByteString::copyFrom).toList()),
                Vector.Builder::setByteVector
        );
    }

}
