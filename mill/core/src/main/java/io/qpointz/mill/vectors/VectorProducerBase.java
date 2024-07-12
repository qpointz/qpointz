package io.qpointz.mill.vectors;

import io.qpointz.mill.proto.Vector;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class VectorProducerBase<T,B> implements VectorProducer<T> {

    private B builder;

    @Getter(AccessLevel.PROTECTED)
    private Vector.NullsVector.Builder nullsBuilder;

    protected VectorProducerBase() {
        this.reset();
    }

    protected abstract B newBuilder();

    protected abstract void append(B builder, T value, boolean isNull);

    protected abstract T getNullValue();

    protected abstract void buildBy(Vector.Builder vectorBuilder, B builder);

    @Override
    public void append(T value, Boolean isNull) {
        this.append(this.builder, isNull ? this.getNullValue() : value, isNull);
    }

    @Override
    public void reset() {
        this.builder = this.newBuilder();
        this.nullsBuilder = Vector.NullsVector.newBuilder();
    }

    @Override
    public void build(Vector.Builder builder) {
        this.buildBy(builder, this.builder);
    }

    public static <K,L> VectorProducerBase<K,L> createProducer (
            Supplier<L> createBuilder,
            K nullValue,
            BiConsumer<L,K> appendConsumer,
            BiConsumer<Vector.Builder, L> buildByConsumer)
    {
        return new VectorProducerBase<K,L>() {

            @Override
            protected K getNullValue() {
                return nullValue;
            }

            @Override
            protected L newBuilder() {
                return createBuilder.get();
            }

            @Override
            protected void append(L builder, K value, boolean isNull) {
                appendConsumer.accept(builder, value);
                this.getNullsBuilder().addNulls(isNull);
            }

            @Override
            protected void buildBy(Vector.Builder vectorBuilder, L builder) {
                vectorBuilder.setNulls(this.getNullsBuilder());
                buildByConsumer.accept(vectorBuilder, builder);
            }

        };
    }

}
