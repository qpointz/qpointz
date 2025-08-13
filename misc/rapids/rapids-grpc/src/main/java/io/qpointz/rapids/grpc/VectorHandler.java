package io.qpointz.rapids.grpc;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class VectorHandler<TType, TVectorType, TVectorBuilderType> {

    private final Supplier<TVectorBuilderType> createNewBuilder;
    private final BiConsumer<TVectorBuilderType, Iterable<TType>> onSetValues;
    private final BiConsumer<TVectorBuilderType, Iterable<Boolean>> onSetNulls;
    private final BiConsumer<Vector.Builder, TVectorBuilderType> onVector;
    private final Function<Vector, TVectorType> getVector;
    private final BiFunction<TVectorType, Integer, Boolean> getVectorNull;
    private final BiFunction<TVectorType, Integer,TType> getVectorValue;

    public VectorHandler(Supplier<TVectorBuilderType> createNewBuilder,
                          BiConsumer<TVectorBuilderType, Iterable<TType>> onSetValues,
                          BiConsumer<TVectorBuilderType, Iterable<Boolean>> onSetNulls,
                          BiConsumer<Vector.Builder, TVectorBuilderType> onVector,
                          Function<Vector, TVectorType> getVector,
                          BiFunction<TVectorType, Integer, Boolean> getVectorNull,
                          BiFunction<TVectorType, Integer, TType> getVectorValue
    ) {
        this.createNewBuilder = createNewBuilder;
        this.onSetValues = onSetValues;
        this.onSetNulls = onSetNulls;
        this.onVector = onVector;
        this.getVector = getVector;
        this.getVectorNull = getVectorNull;
        this.getVectorValue = getVectorValue;

    }

    public TVectorBuilderType newBuilder() {
        return this.createNewBuilder.get();
    }

    public void setValues(TVectorBuilderType typeVectorBuilder, Iterable<TType> values) {
        this.onSetValues.accept(typeVectorBuilder, values);
    }

    public void setNulls(TVectorBuilderType typeVectorBuilder, Iterable<Boolean> nulls) {
        this.onSetNulls.accept(typeVectorBuilder, nulls);
    }

    public void buildVector(Vector.Builder vectorBuilder, TVectorBuilderType typeVectorBuilder) {
        this.onVector.accept(vectorBuilder, typeVectorBuilder);
    }

    public Optional<TType> read(Vector vector, Integer recordIdx) {
        final var cv = this.getVector.apply(vector);
        final var isNull = this.getVectorNull.apply(cv, recordIdx);
        return isNull
            ? Optional.empty()
            : Optional.of(this.getVectorValue.apply(cv, recordIdx));
    }

}
