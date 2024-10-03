package io.qpointz.mill.types.conversion;

public interface ValueConverter<F,T> {
    T to(F value);
    F from(T value);
}
