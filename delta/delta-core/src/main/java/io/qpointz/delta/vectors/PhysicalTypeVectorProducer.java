package io.qpointz.delta.vectors;

import io.qpointz.delta.proto.Vector;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class PhysicalTypeVectorProducer<T,B> implements VectorProducer<T> {

    private B builder;

    @Getter(AccessLevel.PROTECTED)
    private Vector.NullsVector.Builder nullsBuilder;

    protected PhysicalTypeVectorProducer() {
        this.reset();
    }

    protected abstract B newBuilder();

    protected abstract void appendValue(B builder, T value, boolean isNull);

    protected abstract void appendAllValues(B builder, Collection<T> values, List<Boolean> nulls);

    protected abstract T getNullValue();

    protected abstract void addToVector(Vector.Builder vectorBuilder, B builder);

    @Override
    public void append(T value) {
        this.appendValue(this.builder, value, false);
    }

    @Override
    public void appendAll(Collection<T> values) {
        this.appendAllValues(this.builder, values, Collections.nCopies(values.size(), false));
    }

    @Override
    public void appendNull() {
        this.appendValue(this.builder, this.getNullValue(), true);
    }

    @Override
    public void appendNulls(int count) {
        this.appendAllValues(this.builder, Collections.nCopies(count, this.getNullValue()), Collections.nCopies(count, true));
    }

    @Override
    public void reset() {
        this.builder = this.newBuilder();
        this.nullsBuilder = Vector.NullsVector.newBuilder();
    }

    @Override
    public void addToVector(Vector.Builder builder) {
        this.addToVector(builder, this.builder);
    }

    public static <K,L> PhysicalTypeVectorProducer<K,L> createProducer (
            Supplier<L> createBuilder,
            K nullValue,
            BiConsumer<L,K> appendConsumer, BiConsumer<L, Collection<K>> appendAllConsumer,
            BiConsumer<Vector.Builder, L> addToVectorConsumer)
    {
        return new PhysicalTypeVectorProducer<K,L>() {
            @Override
            protected L newBuilder() {
                return createBuilder.get();
            }

            @Override
            protected void appendValue(L builder, K value, boolean isNull) {
                appendConsumer.accept(builder, value);
                this.getNullsBuilder().addNulls(isNull);
            }

            @Override
            protected void appendAllValues(L builder, Collection<K> values, List<Boolean> nulls) {
                appendAllConsumer.accept(builder, values);
                this.getNullsBuilder().addAllNulls(nulls);
            }

            @Override
            protected K getNullValue() {
                return nullValue;
            }

            @Override
            protected void addToVector(Vector.Builder vectorBuilder, L builder) {
                vectorBuilder.setNulls(this.getNullsBuilder());
                addToVectorConsumer.accept(vectorBuilder, builder);
            }

        };
    }

}
