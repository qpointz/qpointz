package io.qpointz.rapids.grpc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class RapidsType<TNativeType, TVectorValueType, TVectorType, TVectorBuilderType>  {

    private final ValueType physicalRapidsType;
    private final ValueType logicalRapidsType;
    private final TNativeType javaNullValue;

    private final VectorHandler<TVectorValueType, TVectorType, TVectorBuilderType> vectorHandler;
    private final boolean isPhisicalType;
    private final Function<TVectorValueType, TNativeType> asNative;
    private final Function<TNativeType, TVectorValueType> asVector;
    private final BiFunction<ResultSet, Integer, TNativeType> fromResultSet;

    protected RapidsType(ValueType physicalRapidsType, ValueType logicalRapidsType,
                         TNativeType javaNullValue,
                         VectorHandler<TVectorValueType, TVectorType, TVectorBuilderType> vectorHandler,
                         Function<TNativeType, TVectorValueType> asVector,
                         Function<TVectorValueType, TNativeType> asNative,
                         BiFunction<ResultSet, Integer, TNativeType> fromResultSet

    ) {
        this.physicalRapidsType = physicalRapidsType;
        this.logicalRapidsType = logicalRapidsType;
        this.javaNullValue = javaNullValue;
        this.vectorHandler = vectorHandler;
        this.isPhisicalType = physicalRapidsType == logicalRapidsType;
        this.asNative = asNative;
        this.asVector = asVector;
        this.fromResultSet = fromResultSet;
    }
    public ValueType phisicalRapidsType() {
        return this.physicalRapidsType;
    }
    public ValueType logicalRapidsType() {
        return this.logicalRapidsType;
    }
    public TNativeType readResultSet(ResultSet resultSet, int columnIndex) throws SQLException {
        return this.fromResultSet.apply(resultSet, columnIndex);
    }
    public TNativeType javaNullValue() {
        return this.javaNullValue;
    }
    public boolean isPhisicalType() {
        return this.isPhisicalType;
    }
    public boolean isLogicalType() {
        return !this.isPhisicalType;
    }
    public TNativeType asNative(TVectorValueType vectorVal) {
        return this.asNative.apply(vectorVal);
    }
    public TVectorValueType asVector(TNativeType nativeVal) {
        return this.asVector.apply(nativeVal);
    }

    public Optional<TNativeType> readVector(VectorBlock vectorBlock, int vectorIdx, int recordIdx) {
        final var v= vectorBlock.getVectors(vectorIdx);
        return readVector(v, recordIdx);
    }

    private Optional<TNativeType> readVector(Vector vector, int recordIdx) {
        final Optional<TVectorValueType> vectorValue = this.vectorHandler.read(vector, recordIdx);
        return vectorValue.isPresent()
                ? Optional.of(this.asNative(vectorValue.get()))
                : Optional.empty();
    }

    public VectorConsumer<TVectorValueType> createConsumer() {
        return new TypeVectorConsumer();
    }

    private class TypeVectorConsumer extends VectorConsumer<TVectorValueType> {

        @Override
        protected void vector(Vector.Builder vectorBuilder, Iterable<TVectorValueType> values, Iterable<Boolean> nulls) {
            final var handler = RapidsType.this.vectorHandler;
            final var nb =  handler.newBuilder();
            handler.setValues(nb, values);
            handler.setNulls(nb, nulls);
            RapidsType.this.vectorHandler.buildVector(vectorBuilder, nb);
        }

        @Override
        protected TVectorValueType getValue(ResultSet resultSet, int columnIndex) throws SQLException {
            return RapidsType.this.asVector.apply(RapidsType.this.fromResultSet.apply(resultSet, columnIndex));
        }

        @Override
        protected TVectorValueType nullValue() {
            return RapidsType.this.asVector.apply(RapidsType.this.javaNullValue());
        }
    }

    public final VectorReader<TNativeType> VECTOR_READER = new TypeVectorReader();

    private class TypeVectorReader extends VectorReader<TNativeType> {
        @Override
        public Optional<TNativeType> read(Vector vector, int recordIdx) {
            final var mayBeVector = RapidsType.this.vectorHandler.read(vector, recordIdx);
            return mayBeVector.isEmpty()
                    ? Optional.empty()
                    : Optional.of(RapidsType.this.asNative.apply(mayBeVector.get()));
        }

        @Override
        public TNativeType valueOrNull(Optional<TNativeType> mayBeValue) {
            return mayBeValue.isPresent()
                    ? mayBeValue.get()
                    : RapidsType.this.javaNullValue;
        }
    }

}
