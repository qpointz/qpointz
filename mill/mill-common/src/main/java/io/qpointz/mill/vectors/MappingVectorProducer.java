package io.qpointz.mill.vectors;

import io.qpointz.mill.proto.Vector;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class MappingVectorProducer<F,T> implements VectorProducer<F> {

    protected abstract T mapValue(F from);

    protected abstract boolean isNull(F from);

    public abstract void append(F value);

    public abstract static class DelegatingMappingVectorProducer<F,T> extends MappingVectorProducer<F,T> {

        private final VectorProducer<T> delegateTo  ;

        protected DelegatingMappingVectorProducer(VectorProducer<T> delegateTo) {
            this.delegateTo = delegateTo;
        }

        @Override
        public void reset() {
            this.delegateTo.reset();
        }

        @Override
        public void append(F value, boolean isNull) {
            this.delegateTo.append(this.mapValue(value), isNull);
        }

        @Override
        public void append(F value) {
            this.delegateTo.append(this.mapValue(value), this.isNull(value));
        }

        @Override
        public void build(Vector.Builder builder) {
            this.delegateTo.build(builder);
        }

    }

    public static <K,L> MappingVectorProducer<K,L> createProducer(VectorProducer<L> producer,
                                                                  Function<K,L> mapFunc, Predicate<K> isNullFunc ) {
        return new DelegatingMappingVectorProducer<K,L>(producer) {

            @Override
            protected L mapValue(K from) {
                return mapFunc.apply(from);
            }

            @Override
            protected boolean isNull(K from) {
                return isNullFunc.test(from);
            }
        };
    }

}
