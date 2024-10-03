package io.qpointz.mill.types.physical;

public interface PhysicalTypeShuttle<T> {
    T visit(BytesPhysical binaryType);

    T visit(BoolPhysical boolType);

    T visit(FP32Physical fp32Type);

    T visit(FP64Physical fp64Type);

    T visit(I32Physical i32Type);

    T visit(I64Physical i64Type);

    T visit(StringPhysical stringType);
}
