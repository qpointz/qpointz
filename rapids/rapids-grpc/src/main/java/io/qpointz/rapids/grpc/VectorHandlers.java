package io.qpointz.rapids.grpc;

import com.google.protobuf.ByteString;

public final class VectorHandlers {
    private VectorHandlers() {
    }

    public static final VectorHandler<Boolean, BoolVector, BoolVector.Builder> BOOLEAN
            = new VectorHandler<>(
                BoolVector::newBuilder,
                BoolVector.Builder::addAllValues,
                BoolVector.Builder::addAllNulls,
                Vector.Builder::setBoolVector,
                Vector::getBoolVector,
                BoolVector::getNulls,
                BoolVector::getValues
            );

    public static final VectorHandler<String, StringVector, StringVector.Builder> STRING
            = new VectorHandler<>(
            StringVector::newBuilder,
            StringVector.Builder::addAllValues,
            StringVector.Builder::addAllNulls,
            Vector.Builder::setStringVector,
            Vector::getStringVector,
            StringVector::getNulls,
            StringVector::getValues
    );

    public static final VectorHandler<Integer, Int32Vector, Int32Vector.Builder> INT32
            = new VectorHandler<>(
            Int32Vector::newBuilder,
            Int32Vector.Builder::addAllValues,
            Int32Vector.Builder::addAllNulls,
            Vector.Builder::setInt32Vector,
            Vector::getInt32Vector,
            Int32Vector::getNulls,
            Int32Vector::getValues
    );

    public static final VectorHandler<Long, Int64Vector, Int64Vector.Builder> INT64
            = new VectorHandler<>(
            Int64Vector::newBuilder,
            Int64Vector.Builder::addAllValues,
            Int64Vector.Builder::addAllNulls,
            Vector.Builder::setInt64Vector,
            Vector::getInt64Vector,
            Int64Vector::getNulls,
            Int64Vector::getValues
    );

    public static final VectorHandler<Double, DoubleVector, DoubleVector.Builder> DOUBLE
            = new VectorHandler<>(
            DoubleVector::newBuilder,
            DoubleVector.Builder::addAllValues,
            DoubleVector.Builder::addAllNulls,
            Vector.Builder::setDoubleVector,
            Vector::getDoubleVector,
            DoubleVector::getNulls,
            DoubleVector::getValues
    );

    public static final VectorHandler<Float, FloatVector, FloatVector.Builder> FLOAT
            = new VectorHandler<>(
            FloatVector::newBuilder,
            FloatVector.Builder::addAllValues,
            FloatVector.Builder::addAllNulls,
            Vector.Builder::setFloatVector,
            Vector::getFloatVector,
            FloatVector::getNulls,
            FloatVector::getValues
    );

    public static final VectorHandler<ByteString, ByteVector, ByteVector.Builder> BYTES
            = new VectorHandler<>(
            ByteVector::newBuilder,
            ByteVector.Builder::addAllValues,
            ByteVector.Builder::addAllNulls,
            Vector.Builder::setByteVector,
            Vector::getByteVector,
            ByteVector::getNulls,
            ByteVector::getValues
            );
}
