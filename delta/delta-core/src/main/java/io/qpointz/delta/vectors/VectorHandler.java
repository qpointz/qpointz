package io.qpointz.delta.vectors;

public interface VectorHandler<T extends VectorProducer> {
    abstract T createProducer();
}